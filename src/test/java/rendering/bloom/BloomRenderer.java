package rendering.bloom;

import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;

import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.internal.shaders.BlurResolveShader;

public class BloomRenderer {
	static final int framebufferId = glGenFramebuffers();
	static final int framebufferAttachmentId = glGenTextures();
	static {
		// replace GLContextState with RenderSystem or whatever mc has for this (or glBindFrameBuffer if it doesn't)
		GLContextState.bindFrameBuffer(framebufferId);
		int[] dims = new int[4];
		glGetIntegerv(GL_VIEWPORT, dims);
		int width = dims[2], height = dims[3];
		screenSize(width, height);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferAttachmentId, 0);
		GLContextState.bindFrameBuffer(0);
		RenderThread.RESIZE.andThen(BloomRenderer::screenSize);
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
		BlurResolveShader shader = BlurResolveShader.INSTANCE;
		shader.tex.tex(framebufferAttachmentId);
		shader.strategy(AutoStrat.QUADS);
		shader.vert().vec3f(-1, -1, 0);
		shader.vert().vec3f(-1, 1, 0);
		shader.vert().vec3f(1, 1, 0);
		shader.vert().vec3f(1, -1, 0);
		shader.draw();
	}
}
