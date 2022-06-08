package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.state.GLContextState;

public final class GLStateBuilder {
	public static final int DEPTH_TEST_SET = 0b1, DEPTH_FUNC_SET = 0b100000, DEPTH_MASK_SET = 0b100000;
	public static final int BLEND_SET = 0b10, BLEND_ALL_SET = 0b100, BLEND_EQ_SET = 0b1000, BLEND_I_SET = 0b10000;

	public static final int DEPTH_TEST = 0b1, DEPTH_MASK = 0b10;
	public static final int BLEND = 0b10000;

	int set, flags;
	int depthFunc;
	int blendEquation;
	int blendSrc, blendDst;
	int[] blendISrc, blendIDst;

	public static GLStateBuilder builder() {
		return new GLStateBuilder();
	}

	public GLStateBuilder() {
		this.depthFunc = GLContextState.DEPTH_FUNC.initialState;
		this.blendEquation = GLContextState.BLEND_EQUATION.initialState;
	}

	public GLStateBuilder(GLStateBuilder builder) {
		this.depthFunc = builder.depthFunc;
		this.blendEquation = builder.blendEquation;
		this.set = builder.set;
		this.flags = builder.flags;
		this.blendISrc = builder.blendISrc;
		this.blendIDst = builder.blendIDst;
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

	private void enabled(int clearFlag, int setFlag) {
		this.set = (this.set & ~clearFlag) | setFlag;
	}

	private void enabled(int flag) {
		this.set = (this.set & ~flag) | flag;
	}

	private void enabled(int flag, boolean enabled) {
		this.set = (this.set & ~flag) | (enabled ? flag : 0);
	}

	private void set(int clearFlag, int setFlag) {
		this.flags = (this.flags & ~clearFlag) | setFlag;
	}

	private void set(int flag) {
		this.flags = (this.flags & ~flag) | flag;
	}

	private void set(int flag, boolean state) {
		this.flags = (this.flags & ~flag) | (state ? flag : 0);
	}

	public boolean isEnabled(int flag) {
		return (this.set & flag) != 0;
	}

	public boolean getBoolean(int flag) {
		return (this.flags & flag) != 0;
	}

	// todo equals


}
