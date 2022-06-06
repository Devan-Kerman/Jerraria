package net.devtech.jerraria.render.internal.buffers;

public abstract class SharedUBOBuilder extends AbstractUBOBuilder {
	public SharedUBOBuilder(
		int unpaddedLen,
		int paddedLen,
		int[] structVariableOffsets,
		int structsStart) {
		super(unpaddedLen, paddedLen, structVariableOffsets, structsStart);
	}

	public SharedUBOBuilder(AbstractBOBuilder buffer) {
		super(buffer);
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
}
