package net.devtech.jerraria.render.internal.buffers;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.devtech.jerraria.render.internal.ConcurrentByteBufferGlDataBuf;

public abstract class SharedUBOBuilder extends AbstractUBOBuilder {
	final Lock bufferCacheLock = new ReentrantLock();
	List<ConcurrentBuf> instances = new ArrayList<>();

	public SharedUBOBuilder(
		int unpaddedLen,
		int paddedLen,
		int[] structVariableOffsets,
		int structsStart) {
		super(unpaddedLen, paddedLen, structVariableOffsets, structsStart, 4);
	}

	public void bind(int bindingPoint, int structIndex) {
		// mark dirty and ensure buffer capacity
		this.flush();
		this.state().bindBufferRange(
			bindingPoint,
			this.glId,
			this.getOffset(structIndex),
			this.structLen
		);
	}

	@Override
	public BufferObjectBuilderAccess struct(int structIndex) {
		super.struct(structIndex);
		final List<ConcurrentBuf> buffers = this.instances;
		if(structIndex < buffers.size()) {
			return buffers.get(structIndex);
		} else {
			Lock lock = this.bufferCacheLock;
			lock.lock();
			try {
				// recheck once lock is acquired
				if(structIndex < buffers.size()) {
					return buffers.get(structIndex);
				}

				List<ConcurrentBuf> copy = new ArrayList<>(this.instances);
				int size;
				while(structIndex >= (size = copy.size())) {
					copy.add(new ConcurrentBuf(size));
				}
				this.instances = copy;
				return copy.get(structIndex);
			} finally {
				lock.unlock();
			}
		}
	}

	public BufferObjectBuilderAccess struct0(int structIndex) {
		return super.struct(structIndex);
	}

	public BufferObjectBuilderAccess structVar0(int structIndex, int varIndex) {
		super.struct(structIndex);
		super.variable(varIndex);
		return SharedUBOBuilder.this;
	}

	public class ConcurrentBuf extends ConcurrentByteBufferGlDataBuf implements BufferObjectBuilderAccess {
		final int structIndex;
		int varIndex;

		public ConcurrentBuf(int index) {
			this.structIndex = index;
		}

		@Override
		public BufferObjectBuilderAccess struct(int structIndex) {
			return SharedUBOBuilder.this.struct(structIndex);
		}

		@Override
		public BufferObjectBuilderAccess variable(int variableIndex) {
			this.pos = SharedUBOBuilder.this.getOffset(this.structIndex, variableIndex);
			this.varIndex = variableIndex;
			return this;
		}


		@Override
		public void copyFrom(BufferObjectBuilderAccess src, int from, int to, int fromOffset, int toOffset, int len) {
			SharedUBOBuilder.this.copyFrom(src, from, to, fromOffset, toOffset, len);
		}

		@Override
		public AbstractBOBuilder getRoot() {
			return SharedUBOBuilder.this;
		}

		@Override
		public void loadFeedback() {
			SharedUBOBuilder.this.struct0(this.structIndex);
			SharedUBOBuilder.this.loadFeedback();
		}

		@Override
		public long uint() {
			return SharedUBOBuilder.this.structVar0(this.structIndex, this.varIndex).uint();
		}

		@Override
		protected ByteBuffer getBuffer() {
			return SharedUBOBuilder.this.buffer;
		}
	}
}
