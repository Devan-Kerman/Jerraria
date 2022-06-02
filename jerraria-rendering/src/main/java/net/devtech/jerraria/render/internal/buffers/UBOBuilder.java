package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL31.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static org.lwjgl.opengl.GL46.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.math.JMath;

public class UBOBuilder extends ByteBufferGlDataBuf {
	public static final int UBO_PADDING = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	public static final int ATOMIC_PADDING = 4;

	final ByteBuffer variableStruct;
	final BitSet initializedVariables;
	public final int[] structIntervals;
	final int unpaddedStructLen, structLen, structsStart;
	List<UBOBuilder> deferred;
	UBOBuilder copy;
	int glId, bufferObjectLen, structIndex;
	ByteBuffer primary;

	public UBOBuilder(int unpaddedLen, int paddedLen, int[] structVariableOffsets, int structsStart) {
		this.variableStruct = ElementBufferBuilder.allocateBuffer(unpaddedLen);
		this.initializedVariables = new BitSet(structVariableOffsets.length);
		this.structsStart = structsStart;
		this.structIntervals = add(structVariableOffsets, unpaddedLen);
		this.structLen = paddedLen;
		this.unpaddedStructLen = unpaddedLen;
	}

	public UBOBuilder(UBOBuilder buffer) {
		// flush data
		buffer.flush();
		this.unpaddedStructLen = buffer.unpaddedStructLen;
		this.structIntervals = buffer.structIntervals;
		int structLen = buffer.structLen;
		this.structLen = structLen;
		this.structsStart = buffer.structsStart;
		this.initializedVariables = new BitSet(buffer.initializedVariables.length());
		this.glId = buffer.glId;
		this.bufferObjectLen = buffer.bufferObjectLen;
		this.structIndex = buffer.structIndex;
		this.variableStruct = ElementBufferBuilder.allocateBuffer(structLen);
		if(buffer.bufferObjectLen != 0) {
			var def = buffer.deferred;
			if(def == null) {
				def = buffer.deferred = new ArrayList<>();
			}
			def.add(this);
			this.copy = buffer;
		}
	}

	public static int[] add(int[] arr, int val) {
		int last = arr.length, copy[] = Arrays.copyOf(arr, last + 1);
		copy[last] = val;
		return copy;
	}

	public void struct(int structIndex) {
		if(this.structIndex != structIndex) {
			// upload original data
			this.uploadStruct();
		}
		this.primary = this.variableStruct;
	}

	public void structElement(int variableIndex) {
		this.initializedVariables.set(variableIndex);
		this.primary = this.variableStruct;
	}

	public void structVariable(int structIndex, int variableIndex) {
		if(this.structIndex != structIndex) {
			// upload original data
			this.uploadStruct();
		}
		this.initializedVariables.set(variableIndex);
		this.primary = this.variableStruct.position(this.structIntervals[variableIndex]);
	}

	public void bind(int index, int structIndex) {
		if(this.structIndex == structIndex) {
			this.flush();
		}
		if(this.bufferObjectLen != 0) {
			// todo configure this thingy, this is different for UBOs
			this.targetState().bindBufferRange(index, this.glId, this.structIndex * this.structLen + this.structsStart, this.structLen);
		}
	}

	public void copyStruct(UBOBuilder src, int from, int to) {
		this.copyStruct(src, from, to, 0, this.structLen);
	}

	public void copyStruct(UBOBuilder src, int from, int to, int offset, int len) {
		int bufferId, readOff;
		if(this.structIndex == to) {
			this.flush();
		}
		if(src.hasStruct(from)) {
			if(src.structIndex == from) {
				src.flush();
			}

			bufferId = src.glId;
			readOff = src.structsStart + from * src.structLen;
		} else {
			bufferId = StaticBuffers.emptyBuffer(this.structLen);
			readOff = 0;
		}

		int writeOff = this.structsStart + to * this.structLen;
		this.ensureBufferObjectCapacity(writeOff + this.structLen);
		this.targetState().bindBuffer(this.glId);
		glBindBuffer(GL_COPY_READ_BUFFER, bufferId);
		glCopyBufferSubData(GL_COPY_READ_BUFFER, this.targetState().type, readOff+offset, writeOff+offset, len);
	}

	public boolean hasStruct(int index) {
		return this.structsStart+(index+1)*this.structLen <= this.bufferObjectLen;
	}

	protected void flush() {
		this.uploadStruct();
	}

	public void uploadIntervals(
		int target, BitSet initialized, ByteBuffer contents, int[] intervals, int offset) {
		int start = -1;
		for(int i = 0; i < intervals.length; i++) {
			int interval = intervals[i];
			if(!initialized.get(i)) {
				if(start != -1) {
					contents.position(start);
					contents.limit(interval);
					glBufferSubData(target, offset + start, contents);
					contents.limit(contents.capacity());
				}
				start = -1;
			} else if(start == -1) {
				start = interval;
			}
		}
		initialized.clear();
	}

	public void ensureBufferObjectCapacity(int bytes) {
		if(this.bufferObjectLen < bytes) {
			int old = this.glId;
			this.copyBuffer(bytes, old, true);
		}
	}

	public void close() {
		if(this.glId != 0) {
			this.targetState().untrackBuffer(this.glId);
			glDeleteBuffers(this.glId);
		}
	}

	protected int padding() {
		return UBO_PADDING;
	}

	protected GLContextState.IndexedBufferTargetState targetState() {
		return GLContextState.UNIFORM_BUFFER;
	}

	private void copyBuffer(int bytes, int old, boolean delete) {
		int new_ = this.glId = glGenBuffers();
		this.targetState().bindBuffer(new_);

		int newLen = JMath.ceil(JMath.nearestPowerOf2(bytes + 1024), this.padding());
		glBufferData(this.targetState().type, newLen, GL_STATIC_DRAW);
		if(old != 0) {
			glBindBuffer(GL_COPY_READ_BUFFER, old);
			glCopyBufferSubData(GL_COPY_READ_BUFFER, this.targetState().type, 0, 0, this.bufferObjectLen);
			if(delete) {
				this.targetState().untrackBuffer(old);
				glDeleteBuffers(old);
			}
		}
		this.bufferObjectLen = newLen;
	}

	void evaluateDeferredCopies() {
		UBOBuilder copy = this.copy;
		if(copy != null) {
			this.copyBuffer(this.bufferObjectLen, copy.glId, false);
			this.copy = null;
		}

		List<UBOBuilder> deferred = this.deferred;
		if(deferred != null) {
			for(UBOBuilder buffer : deferred) {
				buffer.evaluateDeferredCopies();
			}
			this.deferred = null;
		}
	}

	private void uploadStruct() {
		if(!this.initializedVariables.isEmpty()) {
			this.evaluateDeferredCopies();
			this.ensureBufferObjectCapacity(this.structsStart + (this.structIndex + 1) * this.structLen);
			this.targetState().bindBuffer(this.glId);
			this.uploadIntervals(this.targetState().type,
				this.initializedVariables,
				this.variableStruct,
				this.structIntervals,
				this.structsStart + this.structLen * this.structIndex
			);
		}
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.primary;
	}
}
