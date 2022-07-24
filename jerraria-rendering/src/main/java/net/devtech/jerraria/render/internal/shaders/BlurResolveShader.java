package net.devtech.jerraria.render.internal.shaders;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.Id;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL33;

import net.minecraft.client.MinecraftClient;

public class BlurResolveShader extends Shader<Vec3.F<End>> {
	public static final BlurResolveShader INSTANCE = create(Id.create("jerraria", "impl/blur"), BlurResolveShader::new, BlurResolveShader::new);
	public final Tex tex = this.uni(Tex.tex2d("image"));
	public static final int ATTACHMENT = GL33.glGenTextures();
	public static final int FRAMEBUFFER = GL33.glGenFramebuffers();
	static {
		long handle = MinecraftClient.getInstance().getWindow().getHandle();
		AtomicReference<GLFWWindowSizeCallback> ref = new AtomicReference<>();
		GLFWWindowSizeCallbackI callbackI = (window, width, height) -> {
			RenderSystem.bindTexture(ATTACHMENT);
			GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, width, height, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			RenderSystem.texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
			RenderSystem.texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
		};

		GLFWWindowSizeCallback callback = GLFW.glfwSetWindowSizeCallback(handle, (window, width, height) -> {
			ref.get().invoke(window, width, height);
			callbackI.invoke(handle, width, height);
		});
		ref.setPlain(callback);

		// init framebuffer
		int[] dims = new int[4];
		GL33.glGetIntegerv(GL33.GL_VIEWPORT, dims);
		int width0 = dims[2], height0 = dims[3];
		callbackI.invoke(handle, width0, height0); // invoke initial height

		try {
			GlStateManager._glBindFramebuffer(GL33.GL_FRAMEBUFFER, FRAMEBUFFER);
			GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, ATTACHMENT, 0);
		} finally {
			GlStateManager._glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
		}
	}

	public static void renderBloom(Runnable bloomRenderer) {
		int default_ = GLContextState.getDefaultFramebuffer();
		GLContextState.setAndBindDefaultFrameBuffer(FRAMEBUFFER);
		try {
			bloomRenderer.run();
		} finally {
			GLContextState.setAndBindDefaultFrameBuffer(default_);
		}
	}

	public static void drawBloomToMain() {
		BlurResolveShader shader = INSTANCE;
		shader.tex.tex(ATTACHMENT);
		shader.strategy(AutoStrat.QUADS);
		shader.vert().vec3f(-1, -1, 0);
		shader.vert().vec3f(-1, 1, 0);
		shader.vert().vec3f(1, 1, 0);
		shader.vert().vec3f(1, -1, 0);
		shader.draw(BuiltGlState.builder().faceCulling(false).depthTest(false).blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
		renderBloom(() -> {
			GL33.glClearColor(0, 0, 0, 0);
			GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
		});
	}

	protected BlurResolveShader(VFBuilder<End> builder, Object context) {
		super(builder.add(Vec3.f("pos")), context);
	}

	public BlurResolveShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
