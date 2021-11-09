package net.devtech.jerraria.client.render;

import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glBufferData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.devtech.jerraria.util.Log2;
import org.lwjgl.opengl.GL20;

public final class BufferBuilder {
	final int vertexLength;
	int vertexCount;
	ByteBuffer buffer;

	public BufferBuilder(int length) {
		this.vertexLength = length;
		this.buffer = allocateBuffer(1024);
	}

	public BufferBuilder(int expectedSize, int length) {
		this.vertexLength = length;
		// compute the next highest power of 2 of 32-bit v
		this.buffer = allocateBuffer(Log2.nearestPowerOf2(expectedSize));
	}

	int vertexOffset() {
		return this.vertexCount * this.vertexLength;
	}

	BufferBuilder next() {
		// the order here is correct, looks wrong but it's right
		this.vertexCount++;
		this.allocate(this.vertexLength);
		return this;
	}

	void upload() {
		ByteBuffer buffer = this.buffer;
		int lim = buffer.limit();
		buffer.limit(this.vertexCount * this.vertexLength);
		buffer.position(0); // restore position to 0
		glBufferData(GL_ARRAY_BUFFER, buffer, GL20.GL_STATIC_DRAW);
		buffer.limit(lim);
	}

	/**
	 * Ensure the BufferBuilder has enough space to add the given amount of bytes to it
	 */
	ByteBuffer allocate(int bytes) {
		ByteBuffer buffer = this.buffer;
		if(buffer.limit() < bytes) {
			ByteBuffer old = buffer;
			int oldCount = buffer.limit();
			buffer = allocateBuffer(oldCount << 1);
			buffer.put(0, old, 0, oldCount);
			this.buffer = buffer;
		}
		return buffer;
	}

	private static ByteBuffer allocateBuffer(int size) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

}
