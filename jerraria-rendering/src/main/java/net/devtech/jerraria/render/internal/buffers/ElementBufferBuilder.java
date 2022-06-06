package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL31.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL31.glBufferData;
import static org.lwjgl.opengl.GL31.glBufferSubData;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.util.math.JMath;

public final class ElementBufferBuilder extends ByteBufferGlDataBuf {
	final int vertexLength;
	int vertexCount;
	ByteBuffer buffer;

	public ElementBufferBuilder(int vertexLength) {
		this.vertexLength = vertexLength;
		this.buffer = StaticBuffers.allocateBuffer(Math.max(1024, vertexLength * 16));
	}

	public ElementBufferBuilder(ElementBufferBuilder builder) {
		this(builder, builder.vertexCount);
	}

	public ElementBufferBuilder(ElementBufferBuilder builder, int vertices) {
		this.vertexLength = builder.vertexLength;
		this.vertexCount = vertices;
		ByteBuffer buffer = builder.buffer;
		if(buffer != null) {
			int copiedBytes = this.vertexLength * vertices;
			ByteBuffer copy = StaticBuffers.allocateBuffer(JMath.nearestPowerOf2(copiedBytes));
			copy.put(0, buffer, 0, copiedBytes); // account for unflushed
			copy.position(copiedBytes);
			this.buffer = copy;
		}
	}

	public ElementBufferBuilder(int vertexLength, int expectedSize) {
		this.vertexLength = vertexLength;
		// compute the next highest power of 2 of 32-bit v
		this.buffer = StaticBuffers.allocateBuffer(JMath.nearestPowerOf2(expectedSize));
	}

	public void copyVertexes(ElementBufferBuilder builder, int from, int len) {
		if(builder.vertexLength != this.vertexLength) {
			throw new UnsupportedOperationException("cannot copy from " + builder.vertexLength + " to " + this.vertexLength);
		}
		int bytesToCopy = len * this.vertexLength;
		this.allocate(bytesToCopy);
		ByteBuffer src = builder.buffer, current = this.buffer;
		int offset = current.position();
		current.put(offset, src, from * this.vertexLength, bytesToCopy);
		current.position(offset + bytesToCopy);
		this.vertexCount += len;
	}

	public void copyVertex(ElementBufferBuilder builder, int vertexId) {
		int vertexOffset = vertexId * this.vertexLength;
		ByteBuffer buffer = this.buffer;
		buffer.put(buffer.position(), builder.buffer, vertexOffset, this.vertexLength);
	}

	public void reset() {
		this.vertexCount = 0;
		this.buffer.position(0);
	}

	public ElementBufferBuilder next() {
		// the order here is correct, looks wrong but it's right
		this.vertexCount++;
		this.allocate(this.vertexLength);
		this.buffer.position(this.vertexCount * this.vertexLength);
		return this;
	}

	public void upload(int type) {
		ByteBuffer buffer = this.buffer;
		int lim = buffer.limit();
		buffer.limit(this.vertexCount * this.vertexLength);
		int pos = buffer.position();
		buffer.position(0); // restore position to 0
		glBufferData(type, buffer, GL_STATIC_DRAW);
		buffer.limit(lim);
		buffer.position(pos);
	}

	@Override
	public ByteBuffer getBuffer() {
		return this.buffer;
	}

	public int getVertexCount() {
		return this.vertexCount;
	}

	public int vertexOffset() {
		return this.vertexCount * this.vertexLength;
	}

	public void uniformCount() {
		this.vertexCount = 1;
	}

	public void subUpload(int bufferType, long offset, int vertexLimit) {
		if(this.vertexCount > vertexLimit) {
			throw new IllegalStateException("BufferBuilder has more vertices than was allocated for this instance!");
		}
		ByteBuffer buffer = this.buffer;
		int lim = buffer.limit();
		int pos = buffer.position();
		buffer.limit(this.vertexCount * this.vertexLength);
		buffer.position(0); // restore position to 0

		glBufferSubData(bufferType, offset, buffer);
		buffer.limit(lim);
		buffer.position(pos);
	}

	/**
	 * Ensure the BufferBuilder has enough space to add the given amount of bytes to it
	 */
	public ByteBuffer allocate(int neededForNext) {
		ByteBuffer buffer = this.buffer;
		if((buffer.limit() - this.vertexOffset()) < neededForNext) {
			ByteBuffer old = buffer;
			int oldCount = buffer.limit();
			buffer = StaticBuffers.allocateBuffer(oldCount << 1);
			buffer.put(0, old, 0, oldCount);
			buffer.position(old.position());
			this.buffer = buffer;
		}
		return buffer;
	}
}
