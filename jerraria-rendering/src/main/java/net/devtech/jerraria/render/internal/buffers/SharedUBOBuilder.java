package net.devtech.jerraria.render.internal.buffers;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.devtech.jerraria.render.internal.CASByteBufferGlDataBuf;

public abstract class SharedUBOBuilder extends AbstractUBOBuilder {
	final Lock bufferCacheLock = new ReentrantLock();
	List<CASBuf> buffers = new ArrayList<>();

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
		final List<CASBuf> buffers = this.buffers;
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

				List<CASBuf> copy = new ArrayList<>(this.buffers);
				int size;
				while(structIndex >= (size = copy.size())) {
					copy.add(new CASBuf(size));
				}
				this.buffers = copy;
				return copy.get(structIndex);
			} finally {
				lock.unlock();
			}
		}
	}

	public BufferObjectBuilderAccess struct0(int structIndex) {
		return super.struct(structIndex);
	}

	public class CASBuf extends CASByteBufferGlDataBuf implements BufferObjectBuilderAccess {
		final int structIndex;

		public CASBuf(int index) {
			this.structIndex = index;
		}

		@Override
		public BufferObjectBuilderAccess struct(int structIndex) {
			return SharedUBOBuilder.this.struct(structIndex);
		}

		@Override
		public BufferObjectBuilderAccess variable(int variableIndex) {
			this.pos = SharedUBOBuilder.this.getOffset(this.structIndex, variableIndex);
			return this;
		}

		@Override
		public BufferObjectBuilderAccess structVariable(int structIndex, int variableIndex) {
			return this.struct(structIndex).variable(variableIndex);
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
		protected ByteBuffer getBuffer() {
			return SharedUBOBuilder.this.buffer;
		}
	}
}
