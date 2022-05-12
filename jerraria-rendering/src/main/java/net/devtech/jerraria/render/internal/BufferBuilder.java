package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL31.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glBufferData;
import static org.lwjgl.opengl.GL31.glBufferSubData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.devtech.jerraria.util.math.JMath;

public final class BufferBuilder extends ByteBufferGlDataBuf {
	final int vertexLength;
	int vertexCount;
	ByteBuffer buffer;

	public BufferBuilder(int length) {
		this.vertexLength = length;
		this.buffer = allocateBuffer(Math.max(1024, JMath.nearestPowerOf2(length)));
	}

	public BufferBuilder(BufferBuilder builder) {
		this.vertexLength = builder.vertexLength;
		this.vertexCount = builder.vertexCount;
		ByteBuffer buffer = builder.buffer;
		if(buffer != null) {
			int limit = buffer.limit();
			ByteBuffer copy = allocateBuffer(limit);
			copy.put(0, buffer, 0, Math.min(this.vertexLength * (this.vertexCount + 1), limit)); // account for unflushed
			this.buffer = copy;
		}
	}

	public BufferBuilder(int expectedSize, int length) {
		this.vertexLength = length;
		// compute the next highest power of 2 of 32-bit v
		this.buffer = allocateBuffer(JMath.nearestPowerOf2(expectedSize));
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

	void uniformCount() {
		this.vertexCount = 1;
	}

	void upload(boolean isUniform) {
		ByteBuffer buffer = this.buffer;
		int lim = buffer.limit();
		buffer.limit(this.vertexCount * this.vertexLength);
		buffer.position(0); // restore position to 0
		glBufferData(isUniform ? GL_UNIFORM_BUFFER : GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		buffer.limit(lim);
	}

	void subUpload(boolean isUniform, long offset, int vertexLimit) {
		if(this.vertexCount > vertexLimit) {
			throw new IllegalStateException("BufferBuilder has more vertices than was allocated for this instance!");
		}
		ByteBuffer buffer = this.buffer;
		int lim = buffer.limit();
		buffer.limit(this.vertexCount * this.vertexLength);
		buffer.position(0); // restore position to 0
		glBufferSubData(isUniform ? GL_UNIFORM_BUFFER : GL_ARRAY_BUFFER, offset, buffer);
		buffer.limit(lim);
	}


	/**
	 * Ensure the BufferBuilder has enough space to add the given amount of bytes to it
	 */
	ByteBuffer allocate(int bytes) {
		ByteBuffer buffer = this.buffer;
		if(buffer.remaining() < bytes) {
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

	@Override
	protected ByteBuffer getBuffer() {
		return this.buffer;
	}
}
