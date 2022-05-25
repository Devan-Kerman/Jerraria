package net.devtech.jerraria.render.internal.state;

import static org.lwjgl.opengl.GL46.*;

import net.devtech.jerraria.util.math.JMath;

public final class GLContextState {
	public static final IndexedBufferTargetState UNIFORM_BUFFER = new IndexedBufferTargetState(GL_UNIFORM_BUFFER, GL_MAX_UNIFORM_BUFFER_BINDINGS);
	public static final IndexedBufferTargetState ATOMIC_COUNTERS = new IndexedBufferTargetState(GL_ATOMIC_COUNTER_BUFFER, GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS);
	static int currentGlId;
	static int currentVAO;
	// todo framebuffer

	public static void bindProgram(int programId) {
		if(currentGlId != programId) {
			glUseProgram(programId);
			currentGlId = programId;
		}
	}

	public static void bindVAO(int vaoId) {
		if(currentVAO != vaoId) {
			glBindVertexArray(vaoId);
			currentVAO = vaoId;
		}
	}

	public static void untrackVAOIfBound(int[] arrays) {
		int curr = currentVAO;
		for(int array : arrays) {
			if(curr == array) {
				currentVAO = 0;
				break;
			}
		}
	}

	public static final class BufferTargetState {
		final int type;
		int id;

		public BufferTargetState(int type) {
			this.type = type;
		}

		public void bindBuffer(int bufferId) {
			int current = this.id;
			if(current != bufferId) {
				glBindBuffer(this.type, bufferId);
				this.id = bufferId;
			}
		}
	}

	public static final class IndexedBufferTargetState {
		public final int type;
		final long[] ids;
		final int maxBindings;

		public IndexedBufferTargetState(int type, int maxQuery) {
			int maxBindings = glGetInteger(maxQuery);
			this.type = type;
			this.ids = new long[Math.min(maxBindings, 32768)];
			this.maxBindings = maxBindings;
		}

		public void bindBufferRange(int index, int bufferId, int offset, int byteLength) {
			long toBind = JMath.combineInts(bufferId, offset ^ 0b101010101); // prevent offset 0 and index-less bind from conflicting
			long current = this.ids[index];
			if(current != toBind) {
				glBindBufferRange(
					this.type,
					index,
					bufferId,
					offset,
					byteLength
				);
				this.ids[index] = toBind;
			}
		}

		/**
		 * This is not equivalent to bindBufferRange with a zero index! It will bind to the target but not the index!
		 */
		public void bindBufferBase(int index, int bufferId) {
			long current = this.ids[index];
			if(current != bufferId) {
				glBindBuffer(this.type, bufferId);
				this.ids[index] = bufferId;
			}
		}

		public void bindBuffer(int bufferId) {
			this.bindBufferBase(0, bufferId);
		}
	}
}
