package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.glGetBufferSubData;
import static org.lwjgl.opengl.GL42.GL_ATOMIC_COUNTER_BUFFER;

import net.devtech.jerraria.render.internal.state.GLContextState;

/**
 * Atomic Counter Buffer Object Builder
 */
public class ACBOBuilder extends SharedUBOBuilder {
	public static final int ACBO_PADDING = 4;

	public ACBOBuilder(int unpaddedLen, int paddedLen, int[] structVariableOffsets, int structsStart) {
		super(unpaddedLen, paddedLen, structVariableOffsets, structsStart);
	}

	@Override
	protected GLContextState.IndexedBufferTargetState state() {
		return GLContextState.ATOMIC_COUNTERS;
	}

	@Override
	protected int padding() {
		return 4;
	}

	public long readAtomicCounter() {
		this.loadFeedback();
		GLContextState.ATOMIC_COUNTERS.bindBuffer(this.glId);
		int[] buf = new int[1];
		glGetBufferSubData(GLContextState.ATOMIC_COUNTERS.type, (long) this.structIndex * this.structLen + this.structsStart,
			buf);
		return buf[0] & 0xFFFFFFFFL;
	}
}
