package net.devtech.jerraria.render.internal.buffers;

import net.devtech.jerraria.render.internal.state.GLContextState;

public abstract class AbstractUBOBuilder extends AbstractBOBuilder {
	public AbstractUBOBuilder(
		int unpaddedLen,
		int paddedLen,
		int[] structVariableOffsets,
		int structsStart) {
		super(unpaddedLen, paddedLen, structVariableOffsets, structsStart);
	}

	public AbstractUBOBuilder(AbstractBOBuilder buffer) {
		super(buffer);
	}

	protected abstract GLContextState.IndexedBufferTargetState state();

	@Override
	protected int bindTarget() {
		return this.state().type;
	}

	@Override
	protected void bindBuffer(int glId) {
		this.state().bindBuffer(glId);
	}

	@Override
	protected void deleteBuffer(int glId) {
		this.state().deleteBuffer(glId);
	}
}
