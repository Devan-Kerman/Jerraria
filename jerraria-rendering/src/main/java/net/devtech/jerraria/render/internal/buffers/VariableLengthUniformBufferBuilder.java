package net.devtech.jerraria.render.internal.buffers;

import net.devtech.jerraria.render.internal.state.GLContextState;

public class VariableLengthUniformBufferBuilder extends UniformBufferBuilder {
	final UniformBufferBuilder initial;

	public VariableLengthUniformBufferBuilder(
		GLContextState.IndexedBufferTargetState bind,
		int[] elementOffsets,
		int len,
		int binding,
		int paddedLen,
		int startOffset,
		UniformBufferBuilder fixedElements) {
		super(bind, elementOffsets, len, binding, paddedLen);
		this.startOffset = startOffset;
		this.initial = fixedElements;
		this.initial.resizeGlBuffer(0);
		this.glId = this.initial.glId;
	}

	public UniformBufferBuilder getInitial() {
		return this.initial;
	}

	@Override
	protected void resizeGlBuffer0(long oldLen, long newLen) {
		super.resizeGlBuffer0(oldLen, newLen);
		this.initial.glId = this.glId;
	}
}
