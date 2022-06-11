package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGetBufferSubData;
import static org.lwjgl.opengl.GL31.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL31.GL_COPY_WRITE_BUFFER;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL46.glCopyBufferSubData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.util.math.JMath;

public abstract class AbstractBOBuilder extends ByteBufferGlDataBuf implements BufferObjectBuilderAccess, GlData.ReadableBuf {
	public final int[] structIntervals;
	final int unpaddedStructLen, structLen, structsStart;
	List<AbstractBOBuilder> deferred;
	AbstractBOBuilder copy;
	int glId, bufferObjectLen, structIndex;
	volatile ByteBuffer buffer; // todo one buffer per instance for SharedUBOBuilder for multithreaded uniform setting
	final AtomicInteger dirtyFromPos = new AtomicInteger(Integer.MAX_VALUE), dirtyToPos = new AtomicInteger(Integer.MIN_VALUE), maxPos = new AtomicInteger();

	public AbstractBOBuilder(int unpaddedLen, int paddedLen, int[] structVariableOffsets, int structsStart, int expectedCount) {
		this.buffer = BufferUtil.allocateBuffer(structsStart + unpaddedLen * expectedCount);
		this.structsStart = structsStart;
		this.structIntervals = add(structVariableOffsets, unpaddedLen);
		this.structLen = paddedLen;
		this.unpaddedStructLen = unpaddedLen;
	}

	public AbstractBOBuilder(AbstractBOBuilder buffer) {
		this(buffer, buffer.maxPos.get());
	}

	public AbstractBOBuilder(AbstractBOBuilder buffer, int copyCount) {
		this.unpaddedStructLen = buffer.unpaddedStructLen;
		this.structIntervals = buffer.structIntervals;
		this.structLen = buffer.structLen;
		this.structsStart = buffer.structsStart;
		this.structIndex = buffer.structIndex;
		this.maxPos.set(copyCount);
		if(copyCount != 0) {
			var def = buffer.deferred;
			if(def == null) {
				def = buffer.deferred = new ArrayList<>();
			}
			def.add(this);
			this.copy = buffer;
		}
	}

	protected void mut(int structIndex) {
		this.dirtyToPos.getAndAccumulate(structIndex, Math::max);
		this.dirtyFromPos.getAndAccumulate(structIndex, Math::min);
		this.maxPos.getAndAccumulate(structIndex, Math::max);
	}

	@Override
	public BufferObjectBuilderAccess struct(int structIndex) {
		this.mut(structIndex);
		this.ensureNioBufferCapacity(this.getOffset(structIndex), this.getOffset(structIndex + 1));
		this.structIndex = structIndex;
		return this;
	}

	@Override
	public BufferObjectBuilderAccess variable(int variableIndex) {
		this.buffer.position(this.getOffset(this.structIndex, variableIndex));
		return this;
	}



	public void copyFrom(AbstractBOBuilder src, int from, int to) {
		this.copyFrom(src, from, to, 0, 0, this.structLen);
	}

	@Override
	public void copyFrom(BufferObjectBuilderAccess src_, int from, int to, int fromOffset, int toOffset, int len) {
		AbstractBOBuilder src = src_.getRoot();
		// ensure capacity
		int toStart = this.getOffset(to) + toOffset;
		this.ensureNioBufferCapacity(toStart, toStart + len);
		int fromStart = src.getOffset(from) + fromOffset;
		src.ensureNioBufferCapacity(fromStart, fromStart + len);
		this.buffer.put(toStart, src.buffer, fromStart, len);
		this.mut(to);
	}

	public int getGlId() {
		AbstractBOBuilder copy = this.copy;
		if(copy != null) {
			copy.flush();
			return copy.glId;
		} else {
			return this.glId;
		}
	}

	public boolean flush() {
		if(this.dirtyToPos.get() == Integer.MIN_VALUE) {
			return false;
		}

		int from = this.getOffset(this.dirtyFromPos.get());
		int to = this.getOffset(this.dirtyToPos.get() + 1);
		if(to < from) { // concurrency moment
			return false;
		}

		this.ensureNioBufferCapacity(to, to);
		if(!this.ensureBufferObjectCapacity(from, to)) {
			this.bindBuffer(this.glId);
		}
		this.buffer.position(0); // avoid errors
		this.buffer.limit(to);
		this.buffer.position(from);
		glBufferSubData(this.bindTarget(), from, this.buffer);
		this.buffer.clear();
		this.dirtyToPos.set(Integer.MIN_VALUE);
		this.dirtyFromPos.set(Integer.MAX_VALUE);
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
			synchronized(this) {
				buffer = this.buffer;
				if(buffer == null || buffer.capacity() < updateTo) { // check again once lock is acquired, since buffer is never downsized
					this.evaluateDeferredCopies();
					// update immediately because CAS
					ByteBuffer new_ = this.buffer = BufferUtil.allocateBuffer(JMath.nearestPowerOf2(updateTo + 64));
					if(updateFrom != 0 && buffer != null) {
						new_.put(0, buffer, 0, updateFrom);
					}
				}
			}
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

	@Override
	public void loadFeedback() {
		if(!this.flush()) {
			this.bindBuffer(this.getGlId());
		}
		this.buffer.clear();
		this.buffer.limit(Math.min(this.bufferObjectLen, this.buffer.capacity()));
		glGetBufferSubData(this.bindTarget(), 0, this.buffer);
		this.buffer.clear();
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

	@Override
	public long uint() {
		return this.buffer.getInt() & 0xFFFFFFFFL;
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
			int max = this.maxPos.get();
			this.buffer = BufferUtil.reallocateBuffer(copy.buffer, this.getOffset(max + 1));
			this.dirtyFromPos.set(0);
			this.dirtyToPos.accumulateAndGet(max, Math::max);
		}
	}

	public void bake() {
		this.flush();
		this.evaluateDeferredCopies();
		this.buffer = null;
	}

	@Override
	public AbstractBOBuilder getRoot() {
		return this;
	}
}
