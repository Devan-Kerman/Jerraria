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
import net.devtech.jerraria.util.math.JMath;

// todo next is variable length arrays
public class UniformBufferBuilder extends ByteBufferGlDataBuf {
	final GLContextState.IndexedBufferTargetState bind;
	final int[] elementOffsets;
	final BitSet contains;
	final ByteBuffer contents;
	final int binding;
	final int length, paddedLength;
	int startOffset, glId, capacity, blockId;

	public static UniformBufferBuilder atomic_uint(int binding, int len, int paddedLen) {
		return new UniformBufferBuilder(GLContextState.ATOMIC_COUNTERS, new int[] {0}, len, binding, paddedLen);
	}

	public static UniformBufferBuilder uniform(int[] elementOffsets, int binding, int len, int paddedLen) {
		return new UniformBufferBuilder(GLContextState.UNIFORM_BUFFER, elementOffsets, len, binding, paddedLen);
	}

	// todo Struct copying

	public UniformBufferBuilder(GLContextState.IndexedBufferTargetState bind, int[] elementOffsets, int len, int binding, int paddedLen) {
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
			src.upload(false);
		} else if((index + JMath.ceilDiv(byteLen, this.paddedLength)) >= this.capacity) {
			this.resizeGlBuffer(index + byteLen / this.paddedLength);
		}

		if(!(index == this.blockId && byteLen == this.length)) {
			this.upload(true);
		} else {
			this.bind();
		}

		int read;
		if(src != this) {
			read = GL_COPY_READ_BUFFER;
			glBindBuffer(read, src.glId);
		} else {
			read = this.bind.type;
		}
		long elementSize = this.paddedLength;
		glCopyBufferSubData(read,
			this.bind.type,
			off*elementSize+byteOff+this.startOffset,
			index*elementSize+byteOff+this.startOffset,
			byteLen
		);
	}

	public void switchTo(int blockId) {
		if(!this.contains.isEmpty() && blockId != this.blockId) {
			this.upload(false);
		}
		this.resizeGlBuffer(blockId);
		this.blockId = blockId;
	}

	public void bind() {
		int offset = this.paddedLength * this.blockId + this.startOffset;
		if(this.blockId >= this.capacity) {
			this.resizeGlBuffer(this.blockId);
		}
		this.bind.bindBufferRange(this.binding, this.glId, offset, this.length);
	}

	public void upload(boolean forceBind) {
		if(!this.contains.isEmpty()) {
			this.bind();
			int offset = this.paddedLength * this.blockId + this.startOffset;
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
		} else if(forceBind) {
			this.bind();
		}
	}

	protected void resizeGlBuffer(int blockId) {
		if(blockId >= this.capacity) {
			this.resizeGlBuffer0(
				(long)this.capacity * this.paddedLength + this.length,
				(blockId + 1L) * this.paddedLength
			);
			this.capacity = blockId+1;
		}
	}

	protected void resizeGlBuffer0(long oldLen, long newLen) {
		int old = this.glId;
		int new_ = this.glId = glGenBuffers();
		this.bind.bindBuffer(new_);
		glBufferData(this.bind.type, newLen + this.startOffset, GL_STATIC_DRAW);
		if(old != 0) {
			glBindBuffer(GL_COPY_READ_BUFFER, old);
			glCopyBufferSubData(GL_COPY_READ_BUFFER,
				this.bind.type,
				0,
				0,
				oldLen + this.startOffset
			);
			glDeleteBuffers(old);
		}
	}

	@SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
	public long readAtomicCounter() {
		GLContextState.ATOMIC_COUNTERS.bindBuffer(this.glId);
		int[] buf = new int[1];
		glGetBufferSubData(GL_ATOMIC_COUNTER_BUFFER, this.blockId * this.paddedLength + this.startOffset, buf);
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
