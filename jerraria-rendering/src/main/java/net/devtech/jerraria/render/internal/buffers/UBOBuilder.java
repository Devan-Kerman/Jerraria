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
import static org.lwjgl.opengl.GL46.glCopyBufferSubData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.devtech.jerraria.render.api.impl.RenderingEnvironment;
import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.math.JMath;

// todo allow setting uniforms on multiple threads
public class UBOBuilder extends ByteBufferGlDataBuf {
	public static final int UBO_PADDING = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	public final int[] structIntervals;
	final int unpaddedStructLen, structLen, structsStart;
	List<UBOBuilder> deferred;
	UBOBuilder copy;
	int glId, bufferObjectLen, structIndex;
	ByteBuffer buffer;
	int dirtyToPos = Integer.MIN_VALUE, dirtyFromPos = Integer.MAX_VALUE, maxPos;

	public UBOBuilder(int unpaddedLen, int paddedLen, int[] structVariableOffsets, int structsStart) {
		this.buffer = StaticBuffers.allocateBuffer(structsStart + unpaddedLen * 4);
		this.structsStart = structsStart;
		this.structIntervals = add(structVariableOffsets, unpaddedLen);
		this.structLen = paddedLen;
		this.unpaddedStructLen = unpaddedLen;
	}

	public UBOBuilder(UBOBuilder buffer) { // todo fix
		// flush data
		buffer.flush();
		this.unpaddedStructLen = buffer.unpaddedStructLen;
		this.structIntervals = buffer.structIntervals;
		this.structLen = buffer.structLen;
		this.structsStart = buffer.structsStart;
		this.glId = buffer.glId;
		this.structIndex = buffer.structIndex;
		this.maxPos = buffer.maxPos;
		if(buffer.bufferObjectLen != 0) {
			var def = buffer.deferred;
			if(def == null) {
				def = buffer.deferred = new ArrayList<>();
			}
			def.add(this);
			this.copy = buffer;
		}
	}

	public void struct(int structIndex) {
		RenderingEnvironment.validateRenderThread("Modify Uniform Data");
		this.dirtyToPos = Math.max(this.dirtyToPos, structIndex);
		this.dirtyFromPos = Math.min(this.dirtyFromPos, structIndex);
		this.maxPos = Math.max(this.maxPos, structIndex);
		this.ensureNioBufferCapacity(this.getOffset(structIndex), this.getOffset(structIndex+1));
		this.structIndex = structIndex;
	}

	public void structElement(int variableIndex) {
		RenderingEnvironment.validateRenderThread("Modify Uniform Data");
		this.buffer = this.buffer.position(this.getOffset(this.structIndex, variableIndex));
	}

	public void structVariable(int structIndex, int variableIndex) {
		this.struct(structIndex);
		this.structElement(variableIndex);
	}

	public void bind(int bindingPoint, int structIndex) {
		// mark dirty and ensure buffer capacity
		this.flush();
		this.targetState().bindBufferRange(bindingPoint, this.glId, this.getOffset(structIndex), this.structLen);
	}

	public void copyFrom(UBOBuilder src, int from, int to) {
		this.copyFrom(src, from, to, 0, 0, this.structLen);
	}

	public void copyFrom(UBOBuilder src, int from, int to, int fromOffset, int toOffset, int len) {
		// ensure capacity
		int start = this.getOffset(to) + toOffset;
		this.ensureNioBufferCapacity(start, start + len);
		this.buffer.put(start, src.buffer, src.getOffset(from) + fromOffset, len);
		this.dirtyToPos = Math.max(this.dirtyToPos, to);
		this.dirtyFromPos = Math.min(this.dirtyFromPos, to);
	}

	public void flush() {
		if(this.dirtyToPos == Integer.MIN_VALUE) {
			return;
		}

		int from = this.getOffset(this.dirtyFromPos);
		int to = this.getOffset(this.dirtyToPos + 1);
		this.ensureNioBufferCapacity(from, to);
		this.ensureBufferObjectCapacity(from, to);
		this.targetState().bindBuffer(this.glId);
		this.buffer.position(0); // avoid errors
		this.buffer.limit(to);
		this.buffer.position(from);
		glBufferSubData(this.targetState().type, from, this.buffer);
		this.dirtyToPos = Integer.MIN_VALUE;
		this.dirtyFromPos = Integer.MAX_VALUE;
	}

	public void ensureNioBufferCapacity(int updateFrom, int updateTo) {
		ByteBuffer buffer = this.buffer;
		if(buffer == null || buffer.capacity() < updateTo) {
			this.evaluateDeferredCopies();
			ByteBuffer new_ = StaticBuffers.allocateBuffer(JMath.nearestPowerOf2(updateTo + 64));
			if(updateFrom != 0 && buffer != null) {
				new_.put(0, buffer, 0, updateFrom);
			}
			this.buffer = new_;
		}
	}

	public void ensureBufferObjectCapacity(int updateFrom, int updateTo) {
		if(this.bufferObjectLen < updateTo) {
			int old = this.glId;
			int new_ = this.glId = glGenBuffers();
			this.targetState().bindBuffer(new_);
			int newLen = JMath.ceil(JMath.nearestPowerOf2(updateTo + 1024), this.padding());
			glBufferData(this.targetState().type, newLen, GL_STATIC_DRAW);
			if(old != 0) {
				glBindBuffer(GL_COPY_READ_BUFFER, old);
				if(updateFrom != 0) {
					glCopyBufferSubData(GL_COPY_READ_BUFFER, this.targetState().type, 0, 0, updateFrom);
				}
				this.targetState().untrackBuffer(old);
				glDeleteBuffers(old);
			}
			this.bufferObjectLen = newLen;
		}
	}

	public void close() {
		if(this.glId != 0) {
			this.targetState().untrackBuffer(this.glId);
			glDeleteBuffers(this.glId);
		}
	}

	protected int getOffset(int structIndex) {
		return this.structsStart + structIndex * this.structLen;
	}

	protected int getOffset(int structIndex, int variableIndex) {
		return this.getOffset(structIndex) + this.structIntervals[variableIndex];
	}

	protected int padding() {
		return UBO_PADDING;
	}

	protected GLContextState.IndexedBufferTargetState targetState() {
		return GLContextState.UNIFORM_BUFFER;
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.buffer;
	}

	private void evaluateDeferredCopies() {
		this.deferredCopy();
		List<UBOBuilder> deferred = this.deferred;
		if(deferred != null) {
			for(UBOBuilder buffer : deferred) {
				buffer.evaluateDeferredCopies();
			}
			this.deferred = null;
		}
	}

	private void deferredCopy() {
		UBOBuilder copy = this.copy;
		if(copy != null) {
			this.copy = null;
			copy.evaluateDeferredCopies();
			this.buffer = StaticBuffers.reallocateBuffer(copy.buffer, copy.buffer.capacity());
		}
	}

	static int[] add(int[] arr, int val) {
		int last = arr.length, copy[] = Arrays.copyOf(arr, last + 1);
		copy[last] = val;
		return copy;
	}
}
