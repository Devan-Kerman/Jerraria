package net.devtech.jerraria.client.render;

import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

public final class BufferBuilder {
	ByteBuffer buffer;
	int vertexLength;
	int glId;

	public BufferBuilder() {
		this.buffer = allocateBuffer(1024);
	}

	public BufferBuilder(int expectedSize) {
		// compute the next highest power of 2 of 32-bit v
		long v = expectedSize & 0xFFFFFFFFL;
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		this.buffer = allocateBuffer((int) v);
	}

	public int bind(boolean reset) {
		int glId = this.glId;
		if(glId == MemoryUtil.NULL) {
			glId = this.glId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, glId);
		}
		ByteBuffer buffer = this.buffer;
		if(reset) {
			buffer.rewind();
			glBufferData(GL_ARRAY_BUFFER, buffer, GL20.GL_STATIC_DRAW);
		} else {
			int pos = buffer.position();
			buffer.rewind();
			glBufferData(GL_ARRAY_BUFFER, buffer, GL20.GL_STATIC_DRAW);
			buffer.position(pos);
		}
		return glId;
	}

	public void start(int vertexLength) {
		this.reset();
		this.vertexLength = vertexLength;
	}

	public BufferBuilder putBoolean(int offset, boolean value) {
		this.buffer.put(this.buffer.position() + offset, (byte) (value ? 1 : 0));
		return this;
	}

	public BufferBuilder putInt(int offset, int value) {
		this.buffer.putInt(this.buffer.position() + offset, value);
		return this;
	}

	public BufferBuilder putFloat(int offset, float value) {
		this.buffer.putFloat(this.buffer.position() + offset, value);
		return this;
	}

	public BufferBuilder putDouble(int offset, int value) {
		this.buffer.putDouble(this.buffer.position() + offset, value);
		return this;
	}

	public BufferBuilder putUInt(int offset, long value) {
		this.buffer.putInt(this.buffer.position() + offset, (int) value);
		return this;
	}

	public BufferBuilder next() {
		this.buffer.position(this.buffer.position() + this.vertexLength);
		this.allocate(this.vertexLength);
		return this;
	}

	public void reset() {
		this.buffer.rewind();
	}

	private static ByteBuffer allocateBuffer(int size) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

	/**
	 * Ensure the BufferBuilder has enough space to add the given amount of bytes to it
	 */
	public ByteBuffer allocate(int bytes) {
		ByteBuffer buffer = this.buffer;
		if(buffer.remaining() < bytes) {
			ByteBuffer old = buffer;
			int oldCount = buffer.limit();
			buffer = allocateBuffer(oldCount << 1);
			buffer.put(0, old, 0, oldCount);
			buffer.position(old.position());
			this.buffer = buffer;
		}
		return buffer;
	}
}
