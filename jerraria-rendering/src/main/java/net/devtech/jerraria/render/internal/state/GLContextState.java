package net.devtech.jerraria.render.internal.state;

import static org.lwjgl.opengl.GL46.*;

import java.util.Arrays;
import java.util.function.IntConsumer;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.devtech.jerraria.render.api.OpenGLSupport;
import net.devtech.jerraria.util.math.JMath;
import org.lwjgl.opengl.GL46;

public final class GLContextState {
	public static final IndexedBufferTargetState UNIFORM_BUFFER = new IndexedBufferTargetState(GL_UNIFORM_BUFFER, GL_MAX_UNIFORM_BUFFER_BINDINGS);
	public static final IndexedBufferTargetState ATOMIC_COUNTERS = new IndexedBufferTargetState(GL_ATOMIC_COUNTER_BUFFER, GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS);
	public static final IntState DEPTH_FUNC = new IntState(GL46::glDepthFunc, GL_LESS);
	public static final BoolState DEPTH_MASK = new BoolState(GL46::glDepthMask, true);
	public static final EnableState DEPTH_TEST = new EnableState(GL_DEPTH_TEST, false);
	public static final EnableState BLEND = new EnableState(GL_BLEND, false);
	public static final IntState BLEND_EQUATION = new IntState(GL46::glBlendEquation, GL_FUNC_ADD);

	public static final BlendStateI[] BLEND_STATE_IS;
	private static BlendStateI defaultManager;

	static int currentGlId, currentVAO, readFBO, writeFBO;
	// todo framebuffer

	public static void bindFrameBuffer(int frameBufferId) {
		if(readFBO != frameBufferId && writeFBO != frameBufferId) {
			glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
			writeFBO = readFBO = frameBufferId;
		} else if(readFBO != frameBufferId) {
			glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBufferId);
			readFBO = frameBufferId;
		} else if(writeFBO != frameBufferId) {
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, frameBufferId);
			writeFBO = frameBufferId;
		}
	}

	public static void bindDrawFBO(int frameBufferId) {
		if(writeFBO != frameBufferId) {
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, frameBufferId);
			writeFBO = frameBufferId;
		}
	}

	public static void bindReadFBO(int frameBufferId) {
		if(readFBO != frameBufferId) {
			glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBufferId);
			readFBO = frameBufferId;
		}
	}

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

	public static void blendFunc(int src, int dst) {
		boolean diff = false;
		if(BLEND_STATE_IS != null) {
			for(BlendStateI i : BLEND_STATE_IS) {
				diff |= i.src != src || i.dst != dst;
				i.src = src;
				i.dst = dst;
			}
		} else {
			diff = defaultManager.src != src || defaultManager.dst != dst;
		}

		if(diff) {
			glBlendFunc(src, dst);
		}
	}

	public static final class IntState {
		final IntConsumer binder;
		int id;

		public IntState(IntConsumer binder, int default_) {
			this.binder = binder;
			this.id = default_;
		}

		public void set(int id) {
			int current = this.id;
			if(current != id) {
				this.binder.accept(id);
				this.id = id;
			}
		}
	}

	public static final class BoolState {
		final BooleanConsumer binder;
		boolean state;

		public BoolState(BooleanConsumer binder, boolean default_) {
			this.binder = binder;
			this.state = default_;
		}

		public void set(boolean value) {
			boolean current = this.state;
			if(current != value) {
				this.binder.accept(value);
				this.state = value;
			}
		}
	}

	public static final class EnableState {
		final int type;
		boolean state;

		public EnableState(int type, boolean default_) {
			this.type = type;
			this.state = default_;
		}

		public void set(boolean enable) {
			if(this.state ^ enable) {
				if(enable) {
					glEnable(this.type);
				} else {
					glDisable(this.type);
				}
			}
		}
	}

	public static final class BlendStateI {
		final int index;
		int src = GL_ONE, dst = GL_ZERO;

		public BlendStateI(int index) {
			this.index = index;
		}

		public void set(int src, int dst) {
			if(this.src != src || this.dst != dst) {
				glBlendFunci(this.index, src, dst);
				this.src = src;
				this.dst = dst;
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


	static {
		if(OpenGLSupport.BLEND_FUNC_I) {
			BLEND_STATE_IS = new BlendStateI[32];
			Arrays.setAll(BLEND_STATE_IS, BlendStateI::new);
		} else {
			BLEND_STATE_IS = null;
			defaultManager = new BlendStateI(0);
		}
	}
}
