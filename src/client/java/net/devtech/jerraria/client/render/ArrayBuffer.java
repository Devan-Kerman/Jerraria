package net.devtech.jerraria.client.render;

import org.lwjgl.opengl.GL20;

public class ArrayBuffer {
	final int glId;
	final BufferBuilder builder;

	public ArrayBuffer() {
		this.builder = new BufferBuilder();
		int glId = this.glId = GL20.glGenBuffers();
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, glId);
	}

	public void update(BufferUse use) {

		GL20.glBufferData(GL20.GL_ARRAY_BUFFER, this.builder.buffer, use.glFlag);
	}
}
