package net.devtech.jerraria.render.internal.state;

import static org.lwjgl.opengl.GL46.*;

import java.util.Arrays;
import java.util.function.IntConsumer;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.devtech.jerraria.render.api.OpenGLSupport;
import net.devtech.jerraria.util.math.JMath;
import org.lwjgl.opengl.GL46;

// todo untrack binded deleted buffers
public final class GLContextState {
	private static final int[][] BUFFER_ARRAYS;
	public static final IndexedBufferTargetState UNIFORM_BUFFER = new IndexedBufferTargetState(GL_UNIFORM_BUFFER, GL_MAX_UNIFORM_BUFFER_BINDINGS);
	public static final IndexedBufferTargetState ATOMIC_COUNTERS = new IndexedBufferTargetState(GL_ATOMIC_COUNTER_BUFFER, GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS);
	public static final IndexedBufferTargetState SHADER_BUFFER = new IndexedBufferTargetState(GL_SHADER_STORAGE_BUFFER, GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);

	public static final IntState DEPTH_FUNC = new IntState(GL46::glDepthFunc, GL_LESS);
	public static final BoolState DEPTH_MASK = new BoolState(GL46::glDepthMask, true);
	public static final EnableState DEPTH_TEST = new EnableState(GL_DEPTH_TEST, false);
	public static final EnableState BLEND = new EnableState(GL_BLEND, false);
	public static final IntState BLEND_EQUATION = new IntState(GL46::glBlendEquation, GL_FUNC_ADD);
	public static final BlendStateI[] BLEND_STATE_IS; // todo better version of this, we should store a "generic" target
	private static final BlendStateI defaultManager;

	static int currentGlId, currentVAO, readFBO, writeFBO, defaultFBO;

	public static void bindDefaultFrameBuffer() {
		bindFrameBuffer(defaultFBO);
	}

	public static void bindFrameBuffer(int frameBufferId) {
		boolean read = readFBO != frameBufferId;
		boolean write = writeFBO != frameBufferId;
		if(read && write) {
			glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
			writeFBO = readFBO = frameBufferId;
		} else if(read) {
			glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBufferId);
			readFBO = frameBufferId;
		} else if(write) {
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

	public static void untrackVAOIfBound(int vao) {
		int curr = currentVAO;
		if(curr == vao) {
			currentVAO = 0;
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
			defaultManager.src = src;
			defaultManager.dst = dst;
		}

		if(diff) {
			glBlendFunc(src, dst);
		}
	}

	public static void setDefaultFrameBuffer(int defaultFBO) {
		GLContextState.defaultFBO = defaultFBO;
	}

	public static void setAndBindDefaultFrameBuffer(int defaultFBO) {
		setDefaultFrameBuffer(defaultFBO);
		bindDefaultFrameBuffer();
	}

	public static int getDefaultFramebuffer() {
		return defaultFBO;
	}

	public static void drawBuffers(int amount) {
		glDrawBuffers(BUFFER_ARRAYS[amount]);
	}

	public static void untrackFBOIfBound(int buffer) {
		if(writeFBO == buffer) {
			writeFBO = 0;
		}
		if(readFBO == buffer) {
			readFBO = 0;
		}
	}

	public static final class IntState {
		final IntConsumer binder;
		public final int default_;
		int id;

		public IntState(IntConsumer binder, int default_) {
			this.binder = binder;
			this.default_ = this.id = default_;
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
		public final boolean default_;
		boolean state;

		public BoolState(BooleanConsumer binder, boolean default_) {
			this.binder = binder;
			this.default_ = this.state = default_;
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
		public final boolean default_;
		boolean state;

		public EnableState(int type, boolean default_) {
			this.type = type;
			this.default_ = this.state = default_;
		}

		public void set(boolean enable) {
			if(this.state ^ enable) {
				if(enable) {
					glEnable(this.type);
				} else {
					glDisable(this.type);
				}
				this.state = enable;
			}
		}
	}

	public static final class BlendStateI {
		final int index;
		public final int defaultSrc = GL_ONE, defaultDst = GL_ZERO;
		int src = this.defaultSrc, dst = this.defaultDst;

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
		long generic;

		public IndexedBufferTargetState(int type, int maxQuery) {
			int maxBindings = glGetInteger(maxQuery);
			this.type = type;
			this.ids = new long[Math.min(maxBindings, 32768)];
			this.maxBindings = maxBindings;
		}

		public void bindBufferRange(int index, int bufferId, int offset, int byteLength) {
			long ubid = JMath.combineInts(offset ^ 0b101010101, bufferId); // prevent offset 0 and index-less bind from conflicting
			long current = this.ids[index];
			if(current != ubid || this.generic != ubid) {
				glBindBufferRange(
					this.type,
					index,
					bufferId,
					offset,
					byteLength
				);
				this.ids[index] = ubid;
				this.generic = ubid;
			}
		}

		/**
		 * This is not equivalent to bindBufferRange with a zero index! It will bind to the target but not the index!
		 */
		public void bindBufferBase(int index, int bufferId) {
			long current = this.ids[index];
			if(current != bufferId || this.generic != bufferId) {
				glBindBufferBase(this.type, index, bufferId);
				this.ids[index] = bufferId;
				this.generic = bufferId;
			}
		}

		public void bindBuffer(int bufferId) {
			if(this.generic != bufferId) {
				glBindBuffer(this.type, bufferId);
				this.generic = bufferId;
			}
		}

		public void untrackBuffer(int id) {
			if(this.generic == id) {
				this.generic = 0;
			}
			for(int i = 0; i < this.ids.length; i++) {
				if((this.ids[i] & 0xFFFFFFFFL) == id) {
					this.ids[i] = 0;
				}
			}
		}
	}

	static {
		if(OpenGLSupport.BLEND_FUNC_I) {
			BLEND_STATE_IS = new BlendStateI[32];
			defaultManager = null;
			Arrays.setAll(BLEND_STATE_IS, BlendStateI::new);
		} else {
			BLEND_STATE_IS = null;
			defaultManager = new BlendStateI(0);
		}

		BUFFER_ARRAYS = new int[32][];
		for(int i = 0; i < BUFFER_ARRAYS.length; i++) {
			int[] len = new int[i];
			Arrays.setAll(len, v -> v + GL_COLOR_ATTACHMENT0);
			BUFFER_ARRAYS[i] = len;
		}
	}
}
