package net.devtech.jerraria.render.api;

import java.util.Arrays;

import net.devtech.jerraria.render.internal.state.GLContextState;

public final class GLStateBuilder {
	int depthFunc = GLContextState.DEPTH_FUNC.default_;
	int blendEquation = GLContextState.BLEND_EQUATION.default_;
	boolean depthMask = GLContextState.DEPTH_MASK.default_;
	boolean depthTest = GLContextState.DEPTH_TEST.default_;
	boolean blend = GLContextState.BLEND.default_;
	boolean all;
	int[] srcStates, dstStates;

	public static GLStateBuilder builder() {
		return new GLStateBuilder();
	}

	public GLStateBuilder() {
	}

	public GLStateBuilder(GLStateBuilder builder) {
		this.depthFunc = builder.depthFunc;
		this.blendEquation = builder.blendEquation;
		this.depthMask = builder.depthMask;
		this.depthTest = builder.depthTest;
		this.blend = builder.blend;
		this.all = builder.all;
		this.srcStates = builder.srcStates;
		this.dstStates = builder.dstStates;
	}

	public GLStateBuilder blendFunc(int src, int dst) {
		this.srcStates = new int[]{src};
		this.dstStates = new int[]{dst};
		this.all = true;
		return this;
	}

	public GLStateBuilder srcBlendFuncs(int... src) {
		this.srcStates = src;
		this.all = false;
		return this;
	}

	public GLStateBuilder dstBlendFuncs(int... dst) {
		this.dstStates = dst;
		this.all = false;
		return this;
	}

	public GLStateBuilder depthFunc(int depthFunc) {
		this.depthFunc = depthFunc;
		return this;
	}

	public GLStateBuilder blendEquation(int blendEquation) {
		this.blendEquation = blendEquation;
		return this;
	}

	public GLStateBuilder depthMask(boolean depthMask) {
		this.depthMask = depthMask;
		return this;
	}

	public GLStateBuilder depthTest(boolean depthTest) {
		this.depthTest = depthTest;
		return this;
	}

	public GLStateBuilder blend(boolean blend) {
		this.blend = blend;
		return this;
	}

	public BuiltGlState build() {
		return new BuiltGlStateImpl(new GLStateBuilder(this));
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(!(o instanceof GLStateBuilder builder)) {
			return false;
		}

		if(this.depthFunc != builder.depthFunc) {
			return false;
		}
		if(this.blendEquation != builder.blendEquation) {
			return false;
		}
		if(this.depthMask != builder.depthMask) {
			return false;
		}
		if(this.depthTest != builder.depthTest) {
			return false;
		}
		if(this.blend != builder.blend) {
			return false;
		}
		if(this.all != builder.all) {
			return false;
		}
		if(!Arrays.equals(this.srcStates, builder.srcStates)) {
			return false;
		}
		return Arrays.equals(this.dstStates, builder.dstStates);
	}

	@Override
	public int hashCode() {
		int result = this.depthFunc;
		result = 31 * result + this.blendEquation;
		result = 31 * result + (this.depthMask ? 1 : 0);
		result = 31 * result + (this.depthTest ? 1 : 0);
		result = 31 * result + (this.blend ? 1 : 0);
		result = 31 * result + (this.all ? 1 : 0);
		result = 31 * result + Arrays.hashCode(this.srcStates);
		result = 31 * result + Arrays.hashCode(this.dstStates);
		return result;
	}


}
