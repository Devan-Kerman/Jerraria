package net.devtech.jerraria.render.internal.renderhandler.translucent;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL30.glClearBufferfv;
import static org.lwjgl.opengl.GL33.GL_R32F;
import static org.lwjgl.opengl.GL33.GL_RGBA32F;
import static org.lwjgl.opengl.GL33.glBindTexture;
import static org.lwjgl.opengl.GL33.glGenTextures;
import static org.lwjgl.opengl.GL42.glTexStorage2D;

import java.util.List;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.FragOutput;
import net.devtech.jerraria.render.shaders.WBTransResolveShader;

public class WeightedTranslucentRenderer extends AbstractTranslucencyRenderHandler {
	public static final float[] ZEROS = {
		0,
		0,
		0,
		0
	}, ONES = {
		1,
		1,
		1,
		1
	};
	static final BuiltGlState RESOLVE_STATE = GLStateBuilder
		.builder()
		.depthFunc(GL_ALWAYS)
		.blend(true)
		.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
		.build();

	int revealage; // doesn't need to get cleared
	int accum;

	@Override
	protected TranslucentShaderType type() {
		return TranslucentShaderType.SINGLE_PASS;
	}

	@Override
	public void renderResolve() {
		WBTransResolveShader shader = WBTransResolveShader.INSTANCE;
		shader.accum.tex(this.accum);
		shader.reveal.tex(this.revealage);
		List<RenderCall> queue = this.renderQueue;
		for(int i = 0, size = queue.size(); i < size; i++) {
			RenderCall call = queue.get(i);
			TranslucentShader<?> lucent = (TranslucentShader<?>) call.shader();
			this.initialize(lucent);
			this.refreshBuffers(lucent, i);
			call.exec().accept(call.shader());
		}
		this.renderQueue.clear();

		shader.strategy(AutoStrat.QUADS);
		shader.vert().vec3f(0, 0, 0);
		shader.vert().vec3f(0, 1, 0);
		shader.vert().vec3f(1, 1, 0);
		shader.vert().vec3f(1, 0, 0);
		shader.draw(RESOLVE_STATE);
	}

	@Override
	public void frameSize(int width, int height) { // todo dealloc
		this.revealage = allocTranslucencyBuffer(width, height, GL_R32F); // doesn't need to get cleared
		this.accum = allocTranslucencyBuffer(width, height, GL_RGBA32F);
	}

	protected void initialize(TranslucentShader<?> lucent) {
		lucent.singlePassWeighted.accum.tex(this.accum);
		lucent.singlePassWeighted.reveal.tex(this.revealage);
	}

	private static int allocTranslucencyBuffer(int width, int height, int storageType) {
		int tex = glGenTextures();
		int type = DataType.TEXTURE_2D.elementType;
		glBindTexture(type, tex);
		glTexStorage2D(type, 1, storageType, width, height);
		return tex;
	}

	protected void refreshBuffers(Shader<?> shader, int count) {
		if(count == 0) {
			FragOutput outputs = shader.getShader().outputs;
			outputs.bind();
			glClearBufferfv(GL_COLOR, 0, ZEROS);
			glClearBufferfv(GL_COLOR, 1, ONES);
		}
	}
}
