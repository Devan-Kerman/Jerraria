package net.devtech.jerraria.render.internal.renderhandler.translucent;

import static org.lwjgl.opengl.GL42.*;

import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.internal.shaders.LLTransResolveShader;
import net.devtech.jerraria.util.math.JMath;

public class LinkedListTranslucentRenderer extends AbstractTranslucencyRenderer {
	final int clearingFramebuffer = glGenFramebuffers();
	int bufferTex;
	int buffer;
	int headTex;
	long bufferSize;

	public LinkedListTranslucentRenderer() {
	}

	@Override
	public void renderResolve() throws Exception {
		GLContextState.bindFrameBuffer(this.clearingFramebuffer);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.headTex, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		GLContextState.bindDefaultFrameBuffer();

		long counter = 0L;
		for(RenderCall call : this.renderQueue) {
			TranslucentShader<?> shader = (TranslucentShader<?>) call.shader();
			shader.linkedListUniforms.counter.ui(counter);
			shader.linkedListUniforms.translucencyBuffer.tex(this.bufferTex);
			shader.linkedListUniforms.imgListHead.tex(this.headTex);
			call.exec().accept(shader);
			counter = shader.linkedListUniforms.counter.readUnsignedInteger();
		}

		if(counter >= this.bufferSize) {
			if(this.bufferTex != 0) {
				glDeleteTextures(this.bufferTex);
				glDeleteBuffers(this.buffer);
			}
			this.bufferTex = glGenTextures();
			this.buffer = glGenBuffers();
			int newSize = (int) Math.min(counter*16+1024, Integer.MAX_VALUE);
			this.allocateTranslucencyBuffer(JMath.nearestPowerOf2(newSize) & 0xFFFFFFFFL);
		}

		this.clearRenderQueue();

		LLTransResolveShader shader = LLTransResolveShader.INSTANCE;
		shader.translucencyBuffer.tex(this.bufferTex);
		shader.imgListHead.tex(this.headTex);

		shader.strategy(AutoStrat.QUADS);
		shader.vert().vec3f(0, 0, 0);
		shader.vert().vec3f(0, 1, 0);
		shader.vert().vec3f(1, 1, 0);
		shader.vert().vec3f(1, 0, 0);
		shader.draw(WeightedTranslucentRenderer.RESOLVE_STATE);
	}



	@Override
	public void frameSize(int width, int height) {
		if(this.bufferTex != 0) {
			glDeleteTextures(this.bufferTex);
			glDeleteBuffers(this.buffer);
			glDeleteTextures(this.headTex);
		}
		this.bufferTex = glGenTextures();
		this.buffer = glGenBuffers();
		this.headTex = glGenTextures();
		this.allocateTranslucencyBuffer(JMath.nearestPowerOf2(width * height * 16) & 0xFFFFFFFFL); // doesn't need to get cleared
		this.allocateImageListHead(width, height);
	}

	@Override
	protected TranslucentShaderType type() {
		return TranslucentShaderType.LINKED_LIST;
	}


	private void allocateTranslucencyBuffer(long count) {
		int buf = this.buffer;
		int type = DataType.UINT_IMAGE_BUFFER.elementType;
		glBindBuffer(type, buf);
		glBufferData(type, count, GL_STATIC_DRAW);
		glBindTexture(type, this.bufferTex);
		glTexBuffer(type, GL_RGBA32UI, buf);
		this.bufferSize = count;
	}

	private void allocateImageListHead(int width, int height) {
		int type = DataType.UINT_IMAGE_2D.elementType;
		glBindTexture(type, this.headTex);
		glTexStorage2D(type, 1, GL_R32UI, width, height);
	}
}
