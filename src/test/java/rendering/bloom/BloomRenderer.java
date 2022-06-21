package rendering.bloom;

import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.shaders.BlurShader;

public class BloomRenderer {
	static final int framebufferId = glGenFramebuffers();
	static final int framebufferAttachmentId = glGenTextures();
	static {
		GLContextState.bindFrameBuffer(framebufferId); // idk if this has a RenderSystem call for it, u should use that one
		int[] dims = new int[4];
		glGetIntegerv(GL_VIEWPORT, dims);
		int width = dims[2], height = dims[3];
		screenSize(width, height);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferAttachmentId, 0);
	}

	static void screenSize(int width, int height) { // u will also have to call this every time the screen is resized
		glBindTexture(GL_TEXTURE_2D, framebufferAttachmentId);
		glTexImage2D(GL_TEXTURE_2D,
			0,
			GL_RGBA,
			width,
			height,
			0,
			GL_RGBA,
			GL_UNSIGNED_BYTE,
			(ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		GLContextState.drawBuffers(1);
	}

	static void renderBloom(Runnable bloomRenderer) {
		GLContextState.setAndBindDefaultFrameBuffer(framebufferId);
		try {
			bloomRenderer.run();
		} finally {
			GLContextState.setAndBindDefaultFrameBuffer(0);
		}
	}

	static void drawBloomToMain() {
		GLContextState.bindDefaultFrameBuffer();
		BlurShader shader = BlurShader.INSTANCE;
		shader.tex.tex(framebufferAttachmentId);
		shader.strategy(AutoStrat.QUADS);
		shader.vert().vec3f(-1, -1, 0);
		shader.vert().vec3f(-1, 1, 0);
		shader.vert().vec3f(1, 1, 0);
		shader.vert().vec3f(1, -1, 0);
		shader.draw();
	}
}
