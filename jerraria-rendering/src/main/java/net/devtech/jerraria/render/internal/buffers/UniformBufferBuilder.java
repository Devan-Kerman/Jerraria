package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.glGetBufferSubData;
import static org.lwjgl.opengl.GL42.GL_ATOMIC_COUNTER_BUFFER;
import static org.lwjgl.opengl.GL46.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL46.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL46.glBindBuffer;
import static org.lwjgl.opengl.GL46.glBufferData;
import static org.lwjgl.opengl.GL46.glBufferSubData;
import static org.lwjgl.opengl.GL46.glCopyBufferSubData;
import static org.lwjgl.opengl.GL46.glDeleteBuffers;
import static org.lwjgl.opengl.GL46.glGenBuffers;

import java.nio.ByteBuffer;
import java.util.BitSet;

import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.render.internal.state.GLContextState;

public class UniformBufferBuilder extends ByteBufferGlDataBuf {
	final GLContextState.IndexedBufferTargetState bind;
	final int[] elementOffsets;
	final BitSet contains;
	final ByteBuffer contents;
	final int binding;
	final int length, paddedLength;
	int glId, capacity, blockId;

	public static UniformBufferBuilder atomic_uint(int binding, int len, int paddedLen) {
		return new UniformBufferBuilder(GLContextState.ATOMIC_COUNTERS, new int[] {0}, len, binding, paddedLen);
	}

	public static UniformBufferBuilder uniform(int[] elementOffsets, int binding, int len, int paddedLen) {
		return new UniformBufferBuilder(GLContextState.UNIFORM_BUFFER, elementOffsets, len, binding, paddedLen);
	}

	public UniformBufferBuilder(
		GLContextState.IndexedBufferTargetState bind, int[] elementOffsets, int len, int binding, int paddedLen) {
		this.bind = bind;
		this.elementOffsets = elementOffsets;
		this.contains = new BitSet(elementOffsets.length);
		this.contents = ElementBufferBuilder.allocateBuffer(len);
		this.binding = binding;
		this.paddedLength = paddedLen;
		this.length = len;
	}

	public void copyFrom(int index, UniformBufferBuilder src, int off, int len) {
		this.copyFrom(index, src, off, 0, len * this.length);
	}

	public void copyFrom(int index, UniformBufferBuilder src, int off, int byteOff, int byteLen) {
		if(src != this) {
			src.flush();
		} else if(off >= this.capacity) {
			// make space, technically we could just set index to zero but im lazy
			// this is a rare case anyways
			this.switchTo(off);
		}

		if(this.blockId != index) {
			this.switchTo(index);
		} else {
			this.bind(index); // don't bother uploading data if we will overwrite it right after
		}

		int read;
		if(src != this) {
			read = GL_COPY_READ_BUFFER;
			glBindBuffer(read, src.glId);
		} else {
			read = this.bind.type;
		}
		long elementSize = this.paddedLength;
		glCopyBufferSubData(read, this.bind.type, off*elementSize+byteOff, index*elementSize+byteOff, byteLen);
	}

	public void flush() {
		this.upload(this.blockId);
	}

	public void switchTo(int blockId) {
		if(!this.contains.isEmpty()) {
			this.upload(this.blockId);
		}
		this.blockId = blockId;
	}

	public void bind(int blockId) {
		int offset = this.paddedLength * blockId;
		int target = this.bind.type;
		if(blockId >= this.capacity) {
			int old = this.glId;
			int new_ = this.glId = glGenBuffers();
			this.bind.bindBufferRange(this.binding, new_, offset, this.length);
			this.resizeGlBuffer(target, blockId, old);
		} else {
			this.bind.bindBufferRange(this.binding, this.glId, offset, this.length);
		}
	}

	public void upload(int blockId) {
		this.bind(blockId);
		int offset = this.paddedLength * blockId;
		int target = this.bind.type;
		int last = 0, i = 0;
		for(int size = this.elementOffsets.length; i < size; i++) {
			if(!this.contains.get(i) && i != last) {
				this.contents.limit(this.elementOffsets[i]);
				this.contents.position(this.elementOffsets[last]);
				glBufferSubData(target, this.elementOffsets[last] + offset, this.contents);
				last = i + 1;
			}
		}

		if(last != i) {
			this.contents.limit(this.length);
			this.contents.position(last);
			glBufferSubData(target, this.elementOffsets[last] + offset, this.contents);
		}

		this.contents.limit(this.contents.capacity());
		this.contains.clear();
	}

	private void resizeGlBuffer(int target, int blockId, int old) {
		glBufferData(target, this.paddedLength * (blockId + 1L), GL_STATIC_DRAW);
		if(old != 0) {
			glBindBuffer(GL_COPY_READ_BUFFER, old);
			glCopyBufferSubData(GL_COPY_READ_BUFFER, target, 0, 0, (long)this.capacity * this.paddedLength + this.length);
			glDeleteBuffers(old);
		}
	}

	@SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
	public long readAtomicCounter() {
		GLContextState.ATOMIC_COUNTERS.bindBuffer(this.glId);
		int[] buf = new int[1];
		glGetBufferSubData(GL_ATOMIC_COUNTER_BUFFER, this.blockId * this.paddedLength, buf);
		return buf[0] & 0xFFFFFFFFL;
	}

	public ByteBuffer offset(int variableIndex) {
		this.contains.set(variableIndex);
		return this.contents.position(this.elementOffsets[variableIndex]);
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.contents;
	}
}
