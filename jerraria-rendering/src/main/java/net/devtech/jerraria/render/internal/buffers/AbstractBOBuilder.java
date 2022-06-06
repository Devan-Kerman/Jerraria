package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL31.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL31.GL_COPY_WRITE_BUFFER;
import static org.lwjgl.opengl.GL46.glCopyBufferSubData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.devtech.jerraria.render.api.impl.RenderingEnvironment;
import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.util.math.JMath;

// todo allow setting uniforms on multiple threads
public abstract class AbstractBOBuilder extends ByteBufferGlDataBuf {
	public final int[] structIntervals;
	final int unpaddedStructLen, structLen, structsStart;
	List<AbstractBOBuilder> deferred;
	AbstractBOBuilder copy;
	int glId, bufferObjectLen, structIndex;
	ByteBuffer buffer;
	int dirtyToPos = Integer.MIN_VALUE, dirtyFromPos = Integer.MAX_VALUE, maxPos;

	// todo add expectedCount
	public AbstractBOBuilder(int unpaddedLen, int paddedLen, int[] structVariableOffsets, int structsStart) {
		this.buffer = BufferUtil.allocateBuffer(structsStart + unpaddedLen * 4);
		this.structsStart = structsStart;
		this.structIntervals = add(structVariableOffsets, unpaddedLen);
		this.structLen = paddedLen;
		this.unpaddedStructLen = unpaddedLen;
	}

	public AbstractBOBuilder(AbstractBOBuilder buffer) {
		this(buffer, buffer.maxPos);
	}

	public AbstractBOBuilder(AbstractBOBuilder buffer, int copyCount) { // todo fix
		this.unpaddedStructLen = buffer.unpaddedStructLen;
		this.structIntervals = buffer.structIntervals;
		this.structLen = buffer.structLen;
		this.structsStart = buffer.structsStart;
		this.glId = buffer.glId;
		this.structIndex = buffer.structIndex;
		this.maxPos = copyCount;
		if(copyCount != 0) {
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
		this.ensureNioBufferCapacity(this.getOffset(structIndex), this.getOffset(structIndex + 1));
		this.structIndex = structIndex;
	}

	public void variable(int variableIndex) {
		RenderingEnvironment.validateRenderThread("Modify Uniform Data");
		this.buffer.position(this.getOffset(this.structIndex, variableIndex));
	}

	public void structVariable(int structIndex, int variableIndex) {
		this.struct(structIndex);
		this.variable(variableIndex);
	}

	public void copyFrom(AbstractBOBuilder src, int from, int to) {
		this.copyFrom(src, from, to, 0, 0, this.structLen);
	}

	public void copyFrom(AbstractBOBuilder src, int from, int to, int fromOffset, int toOffset, int len) {
		// ensure capacity
		int toStart = this.getOffset(to) + toOffset;
		this.ensureNioBufferCapacity(toStart, toStart + len);
		int fromStart = src.getOffset(from) + fromOffset;
		src.ensureNioBufferCapacity(fromStart, fromStart + len);
		this.buffer.put(toStart, src.buffer, fromStart, len);
		this.dirtyToPos = Math.max(this.dirtyToPos, to);
		this.maxPos = Math.max(this.maxPos, to);
		this.dirtyFromPos = Math.min(this.dirtyFromPos, to);
	}

	public boolean flush() {
		if(this.dirtyToPos == Integer.MIN_VALUE) {
			AbstractBOBuilder copy = this.copy;
			if(copy != null) {
				copy.flush();
				this.glId = copy.glId;
			}
			return false;
		}

		int from = this.getOffset(this.dirtyFromPos);
		int to = this.getOffset(this.dirtyToPos + 1);
		this.ensureNioBufferCapacity(from, to);
		if(!this.ensureBufferObjectCapacity(from, to)) {
			this.bindBuffer(this.glId);
		}
		this.buffer.position(0); // avoid errors
		this.buffer.limit(to);
		this.buffer.position(from);
		glBufferSubData(this.bindTarget(), from, this.buffer);
		this.buffer.clear();
		this.dirtyToPos = Integer.MIN_VALUE;
		this.dirtyFromPos = Integer.MAX_VALUE;
		return true;
	}


	public void close() {
		if(this.glId != 0) {
			this.deleteBuffer(this.glId);
		}
	}

	static int[] add(int[] arr, int val) {
		int last = arr.length, copy[] = Arrays.copyOf(arr, last + 1);
		copy[last] = val;
		return copy;
	}

	private void ensureNioBufferCapacity(int updateFrom, int updateTo) {
		ByteBuffer buffer = this.buffer;
		if(buffer == null || buffer.capacity() < updateTo) {
			this.evaluateDeferredCopies();
			ByteBuffer new_ = BufferUtil.allocateBuffer(JMath.nearestPowerOf2(updateTo + 64));
			if(updateFrom != 0 && buffer != null) {
				new_.put(0, buffer, 0, updateFrom);
			}
			this.buffer = new_;
		}
	}

	private boolean ensureBufferObjectCapacity(int updateFrom, int updateTo) {
		if(this.bufferObjectLen < updateTo) {
			int old = this.glId;
			int new_ = this.glId = glGenBuffers();
			int type = this.bindTarget();
			this.bindBuffer(new_);
			int newLen = JMath.ceil(JMath.nearestPowerOf2(updateTo + 1024), this.padding());
			glBufferData(type, newLen, GL_STATIC_DRAW);
			if(old != 0) {
				glBindBuffer(GL_COPY_READ_BUFFER, old);
				if(updateFrom != 0) {
					glCopyBufferSubData(GL_COPY_READ_BUFFER, type, 0, 0, updateFrom);
				}
				this.deleteBuffer(old);
			}
			this.bufferObjectLen = newLen;
			return true;
		} else {
			return false;
		}
	}

	protected int getOffset(int structIndex) {
		return this.structsStart + structIndex * this.structLen;
	}

	protected int getOffset(int structIndex, int variableIndex) {
		return this.getOffset(structIndex) + this.structIntervals[variableIndex];
	}

	protected abstract int padding();

	protected void deleteBuffer(int glId) {
		glDeleteBuffers(glId);
	}

	protected void bindBuffer(int glId) {
		glBindBuffer(this.bindTarget(), glId);
	}

	protected int bindTarget() {
		return GL_COPY_WRITE_BUFFER;
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.buffer;
	}

	private void evaluateDeferredCopies() {
		this.deferredCopy();
		List<AbstractBOBuilder> deferred = this.deferred;
		if(deferred != null) {
			for(AbstractBOBuilder buffer : deferred) {
				buffer.evaluateDeferredCopies();
			}
			this.deferred = null;
		}
	}

	private void deferredCopy() {
		AbstractBOBuilder copy = this.copy;
		if(copy != null) {
			this.copy = null;
			copy.evaluateDeferredCopies();
			this.buffer = BufferUtil.reallocateBuffer(copy.buffer, this.getOffset(this.maxPos + 1));
		}
	}
}
