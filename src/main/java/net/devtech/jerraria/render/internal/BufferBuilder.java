package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glBufferData;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import it.unimi.dsi.fastutil.Arrays;
import net.devtech.jerraria.util.Log2;
import org.lwjgl.opengl.GL20;

public final class BufferBuilder extends ByteBufferGlDataBuf {
	final int vertexLength;
	int vertexCount;
	ByteBuffer buffer;

	public BufferBuilder(int length) {
		this.vertexLength = length;
		this.buffer = allocateBuffer(Math.max(1024, Log2.nearestPowerOf2(length)));
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
		this.buffer = allocateBuffer(Log2.nearestPowerOf2(expectedSize));
	}

	/*public interface VertexComparator {
		int compareTo(float[] primitiveA, float[] primitiveB);
	}

	public void sortQuads(VertexComparator comparator, GlData.Element vertex, int vertexes) {
		VAO.Element element = (VAO.Element) vertex; // todo sorts the vertices and not the shapes, uh cope
		if(element.type() != DataType.F32_VEC3) {
			throw new UnsupportedOperationException("Coordinates must be of type " + DataType.F32_VEC3);
		}

		int len = this.vertexLength;
		byte[] swapBuf = new byte[len];
		ByteBuffer buffer = this.buffer;
		Arrays.quickSort(0, this.vertexCount, (k1, k2) -> {
			int indexA = k1 * len * vertexes;
			buffer.get(indexA, bufA);
			int indexB = k2 * len * vertexes;
			return comparator.compareTo(x1, y1, z1, x2, y2, z2);
		}, (a, b) -> {
			buffer.get(len * a, swapBuf, 0, len); // move bytes from a into temp array
			buffer.put(len * a, buffer, len * b, len); // copy from a -> b
			buffer.put(len * a, swapBuf);
		});
	}*/

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
		glBufferData(isUniform ? GL_UNIFORM_BUFFER : GL_ARRAY_BUFFER, buffer, GL20.GL_STATIC_DRAW);
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

	@Override
	protected ByteBuffer getBuffer() {
		return this.buffer;
	}
}
