package net.devtech.jerraria.render.internal.renderhandler.translucent;

import static org.lwjgl.opengl.GL42.*;

import java.util.ArrayList;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.shaders.LLTransResolveShader;
import net.devtech.jerraria.util.math.JMath;

public class LinkedListTranslucentRenderer extends AbstractTranslucencyRenderHandler {
	int clearingFramebuffer = glGenFramebuffers();
	int translucencyBufferTex, translucencyBuffer;
	int imageListHead;

	@Override
	protected TranslucentShaderType type() {
		return TranslucentShaderType.LINKED_LIST;
	}

	@Override
	public void renderResolve() throws Exception {
		GLContextState.bindFrameBuffer(this.clearingFramebuffer);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.imageListHead, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		long counter = 0L;
		for(RenderCall call : this.renderQueue) {
			TranslucentShader<?> shader = (TranslucentShader<?>) call.shader();
			shader.linkedListUniforms.counter.ui(counter);
			shader.linkedListUniforms.translucencyBuffer.tex(this.translucencyBufferTex);
			shader.linkedListUniforms.imgListHead.tex(this.imageListHead);
			call.exec().accept(shader);
			counter = shader.linkedListUniforms.counter.read();
		}

		glMemoryBarrier(-1);
		this.clearRenderQueue();

		LLTransResolveShader shader = LLTransResolveShader.INSTANCE;
		shader.translucencyBuffer.tex(this.translucencyBufferTex);
		shader.imgListHead.tex(this.imageListHead);

		shader.strategy(AutoStrat.QUADS);
		shader.vert().vec3f(0, 0, 0);
		shader.vert().vec3f(0, 1, 0);
		shader.vert().vec3f(1, 1, 0);
		shader.vert().vec3f(1, 0, 0);
		shader.draw(WeightedTranslucentRenderer.RESOLVE_STATE);
	}

	@Override
	public void frameSize(int width, int height) { // todo dealloc
		if(this.translucencyBufferTex != 0) {
			glDeleteTextures(this.translucencyBufferTex);
			glDeleteTextures(this.imageListHead);
			glDeleteBuffers(this.translucencyBuffer);
		}
		this.allocateTranslucencyBuffer(width, height, 16); // doesn't need to get cleared
		this.allocateImageListHead(width + 2, height + 2);
	}

	private void allocateTranslucencyBuffer(int width, int height, int K) {
		int tex = glGenTextures();
		int buf = glGenBuffers();
		int type = DataType.UINT_IMAGE_BUFFER.elementType;
		glBindBuffer(type, buf);
		int size = JMath.nearestPowerOf2(width * height * K * 16);
		glBufferData(type, size, GL_STATIC_DRAW);
		glBindTexture(type, tex);
		glTexBuffer(type, GL_RGBA32UI, buf);
		this.translucencyBufferTex = tex;
		this.translucencyBuffer = buf;
	}

	private void allocateImageListHead(int width, int height) {
		int tex = glGenTextures();
		int type = DataType.UINT_IMAGE_2D.elementType;
		glBindTexture(type, tex);
		glTexStorage2D(type, 1, GL_R32UI, width, height);
		this.imageListHead = tex;
	}
}
