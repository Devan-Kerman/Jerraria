package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;

import net.devtech.jerraria.render.internal.state.GLContextState;


public class UBOBuilder extends SharedUBOBuilder {
	public static final int UBO_PADDING = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

	public UBOBuilder(int unpaddedLen, int paddedLen, int[] structVariableOffsets, int structsStart) {
		super(unpaddedLen, paddedLen, structVariableOffsets, structsStart);
	}

	@Override
	protected GLContextState.IndexedBufferTargetState state() {
		return GLContextState.UNIFORM_BUFFER;
	}

	@Override
	protected int padding() {
		return UBO_PADDING;
	}
}
