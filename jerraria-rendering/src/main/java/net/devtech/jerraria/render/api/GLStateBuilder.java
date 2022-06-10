package net.devtech.jerraria.render.api;

import java.util.Arrays;

import net.devtech.jerraria.render.internal.state.GLContextState;

public final class GLStateBuilder implements BuiltGlState {
	public static final int DEPTH_TEST_SET = 0b1, DEPTH_FUNC_SET = 0b1000000, DEPTH_MASK_SET = 0b100000, FACE_CULLING_SET = 0b10000000;
	public static final int BLEND_SET = 0b10, BLEND_ALL_SET = 0b100, BLEND_EQ_SET = 0b1000, BLEND_I_SET = 0b10000;

	public static final int DEPTH_TEST = 0b1, DEPTH_MASK = 0b10, BLEND = 0b100, FACE_CULLING = 0b1000;

	int set, flags;
	int depthFunc;
	int blendEquation;
	int blendSrc, blendDst;
	int[] blendISrc, blendIDst;

	public GLStateBuilder() {
	}

	public GLStateBuilder(GLStateBuilder builder) {
		this.depthFunc = builder.depthFunc;
		this.blendEquation = builder.blendEquation;
		this.set = builder.set;
		this.flags = builder.flags;
		this.blendISrc = builder.blendISrc;
		this.blendIDst = builder.blendIDst;
		this.blendSrc = builder.blendSrc;
		this.blendDst = builder.blendDst;
	}

	public static GLStateBuilder builder() {
		return new GLStateBuilder();
	}

	public GLStateBuilder unsetBlend() {
		this.enabled(BLEND_SET, false);
		return this;
	}

	public GLStateBuilder defaultBlend() {
		this.enabled(BLEND_SET, GLContextState.BLEND.initialState);
		return this;
	}

	public GLStateBuilder blend(boolean blend) {
		this.enabled(BLEND_SET);
		this.set(BLEND, blend);
		return this;
	}

	public GLStateBuilder unsetFaceCulling() {
		this.enabled(FACE_CULLING_SET, false);
		return this;
	}

	public GLStateBuilder defaultFaceCulling() {
		this.enabled(FACE_CULLING_SET, GLContextState.FACE_CULLING.initialState);
		return this;
	}

	public GLStateBuilder faceCulling(boolean culling) {
		this.enabled(FACE_CULLING_SET);
		this.set(FACE_CULLING, culling);
		return this;
	}

	public GLStateBuilder unsetBlendEq() {
		this.enabled(BLEND_EQ_SET, false);
		return this;
	}

	public GLStateBuilder defaultBlendEq() {
		this.blendEquation = GLContextState.BLEND_EQUATION.initialState;
		this.enabled(BLEND_EQ_SET);
		return this;
	}

	public GLStateBuilder blendEq(int blendEquation) {
		this.blendEquation = blendEquation;
		this.enabled(BLEND_EQ_SET);
		return this;
	}

	public GLStateBuilder unsetBlendFunc() {
		this.enabled(BLEND_ALL_SET, false);
		return this;
	}

	public GLStateBuilder defaultBlendFunc() {
		this.blendSrc = GLContextState.BLEND_ALL_INTERNAL.initialSrc;
		this.blendDst = GLContextState.BLEND_ALL_INTERNAL.initialDst;
		this.enabled(BLEND_ALL_SET);
		return this;
	}

	public GLStateBuilder blendFunc(int src, int dst) {
		this.blendSrc = src;
		this.blendDst = dst;
		this.enabled(BLEND_ALL_SET);
		return this;
	}

	public GLStateBuilder unsetBlendFuncI() {
		this.blendISrc = this.blendIDst = null;
		this.enabled(BLEND_I_SET, false);
		return this;
	}

	public GLStateBuilder defaultBlendFuncI() {
		this.unsetBlendFuncI();
		this.defaultBlendFunc();
		return this;
	}

	/**
	 * @param src 0 for unset
	 */
	public GLStateBuilder srcBlendFuncs(int... src) {
		this.blendISrc = src;
		this.enabled(BLEND_I_SET);
		return this;
	}

	/**
	 * @param dst 0 for unset
	 */
	public GLStateBuilder dstBlendFuncs(int... dst) {
		this.blendIDst = dst;
		this.enabled(BLEND_I_SET);
		return this;
	}

	public GLStateBuilder unsetDepthTest() {
		this.enabled(DEPTH_TEST_SET, false);
		return this;
	}

	public GLStateBuilder defaultDepthTest() {
		this.enabled(DEPTH_TEST_SET);
		this.set(DEPTH_TEST, GLContextState.DEPTH_TEST.initialState);
		return this;
	}

	public GLStateBuilder depthTest(boolean depthTest) {
		this.enabled(DEPTH_TEST_SET);
		this.set(DEPTH_TEST, depthTest);
		return this;
	}

	public GLStateBuilder unsetDepthFunc() {
		this.enabled(DEPTH_FUNC_SET, false);
		return this;
	}

	public GLStateBuilder defaultDepthFunc() {
		this.depthFunc = GLContextState.DEPTH_FUNC.initialState;
		this.enabled(DEPTH_FUNC_SET);
		return this;
	}

	public GLStateBuilder depthFunc(int depthFunc) {
		this.depthFunc = depthFunc;
		this.enabled(DEPTH_FUNC_SET);
		return this;
	}

	public GLStateBuilder unsetDepthMask() {
		this.enabled(DEPTH_MASK_SET, false);
		return this;
	}

	public GLStateBuilder defaultDepthMask() {
		this.enabled(DEPTH_MASK_SET);
		this.set(DEPTH_MASK, GLContextState.DEPTH_TEST.initialState);
		return this;
	}

	public GLStateBuilder depthMask(boolean depthMask) {
		this.enabled(DEPTH_MASK_SET);
		this.set(DEPTH_MASK, depthMask);
		return this;
	}

	public BuiltGlState build() {
		return new BuiltGlStateImpl(new GLStateBuilder(this));
	}

	/**
	 * Applies the current builder to the current gl context, you should restore the state by calling {@link GlStateStack#close()}
	 *
	 * <pre>{@code
	 *  try(GlStateStack stack = GLStateBuilder.builder().depthMask(false).apply()) {
	 *      // disables writing to the depth buffer for all shaders
	 *          // unless they overwrite it with a custom BuiltGlState
	 *          // or a second stack is created
	 *
	 *      // ...
	 *  }
	 * }</pre>
	 */
	public GlStateStack apply() {
		return new GlStateStackImpl(this);
	}

	public GlStateStack applyAndCopy() {
		return new GlStateStackImpl(new GLStateBuilder(this));
	}

	@Override
	public void applyState() {
		BuiltGlStateImpl.apply(this);
	}

	@Override
	public GLStateBuilder copyToBuilder() {
		return new GLStateBuilder(this);
	}

	@Override
	public BuiltGlState copy() {
		return this.build();
	}

	public boolean isEnabled(int flag) {
		return (this.set & flag) != 0;
	}

	public boolean getBoolean(int flag) {
		return (this.flags & flag) != 0;
	}

	@Override
	public int hashCode() {
		int result = this.set;
		result = 31 * result + this.flags;
		result = 31 * result + this.depthFunc;
		result = 31 * result + this.blendEquation;
		result = 31 * result + this.blendSrc;
		result = 31 * result + this.blendDst;
		result = 31 * result + Arrays.hashCode(this.blendISrc);
		result = 31 * result + Arrays.hashCode(this.blendIDst);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(!(o instanceof GLStateBuilder builder)) {
			return false;
		}

		if(this.set != builder.set) {
			return false;
		}
		if(this.flags != builder.flags) {
			return false;
		}
		if(this.depthFunc != builder.depthFunc) {
			return false;
		}
		if(this.blendEquation != builder.blendEquation) {
			return false;
		}
		if(this.blendSrc != builder.blendSrc) {
			return false;
		}
		if(this.blendDst != builder.blendDst) {
			return false;
		}
		if(!Arrays.equals(this.blendISrc, builder.blendISrc)) {
			return false;
		}
		return Arrays.equals(this.blendIDst, builder.blendIDst);
	}

	private void enabled(int flag) {
		this.set = (this.set & ~flag) | flag;
	}

	private void enabled(int flag, boolean enabled) {
		this.set = (this.set & ~flag) | (enabled ? flag : 0);
	}

	private void set(int flag, boolean state) {
		this.flags = (this.flags & ~flag) | (state ? flag : 0);
	}
}
