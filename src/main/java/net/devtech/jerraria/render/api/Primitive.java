package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.VAO;
import org.lwjgl.opengl.GL11;

public class Primitive<T extends ShaderStage> extends ShaderStage implements ShaderStage.Universal {
	ShaderImpl shader;
	Supported active;
	int vertices;

	public enum Supported {
		TRIANGLE(GL11.GL_TRIANGLES, 3);

		final int glId;
		final int vertexCount;

		Supported(int id, int count) {
			this.glId = id;
			this.vertexCount = count;
		}
	}

	void next(Supported supported) {
		// when next is called with a different primitive, flush and start building new array
		// when next is called with a different vao, render the bareshader

		((VAO)this.data).next();
		this.vertices++;

		if(this.active == null) {
			if(supported != null) {
				shader.shader.vao.start();
			}
			this.active = supported;
		} else if(this.active != supported) {
			int vert = this.active.vertexCount;
			if(vert != -1 && this.vertices % vert != 0) {
				throw new UnsupportedOperationException("Expected multiple of " + vert + " vertices for " + this.active + " found " + this.vertices);
			}
			shader.shader.draw(this.active.glId); // flush old primitives
			if(supported != null) {
				shader.shader.vao.start(); // start next primitives
			}
			this.active = supported;
		}
	}

	public T tri() {
		// we need to flush if different primitive
		return null;
	}

	void flush() {

	}
}
