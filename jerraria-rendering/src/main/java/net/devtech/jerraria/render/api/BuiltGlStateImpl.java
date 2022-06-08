package net.devtech.jerraria.render.api;

import static net.devtech.jerraria.render.api.GLStateBuilder.*;

import net.devtech.jerraria.render.internal.state.GLContextState;

record BuiltGlStateImpl(GLStateBuilder state) implements BuiltGlState {
	public static void apply(GLStateBuilder builder) {
		boolean blend;
		if(builder.isEnabled(BLEND_SET)) {
			blend = GLContextState.BLEND.set(builder.getBoolean(BLEND));
		} else {
			blend = GLContextState.BLEND.setToDefault();
		}

		if(blend) {
			if(builder.isEnabled(BLEND_EQ_SET)) {
				GLContextState.BLEND_EQUATION.set(builder.blendEquation);
			} else {
				GLContextState.BLEND_EQUATION.setToDefault();
			}

			boolean blendAll = builder.isEnabled(BLEND_ALL_SET);
			if(blendAll) {
				GLContextState.blendFunc(builder.blendSrc, builder.blendDst);
			} else {
				GLContextState.blendFunc(GLContextState.BLEND_ALL_INTERNAL.defaultSrc, GLContextState.BLEND_ALL_INTERNAL.defaultDst);
			}

			//region blend_i
			if(builder.isEnabled(BLEND_I_SET)) {
				int[] srcs = builder.blendISrc, dsts = builder.blendIDst;
				for(int i = 0; i < Math.max(srcs.length, dsts.length); i++) {
					//noinspection DuplicatedCode
					int src = i < srcs.length ? srcs[i] : 0;
					int dst = i < dsts.length ? dsts[i] : 0;
					GLContextState.BlendStateI state = GLContextState.BLEND_STATE_IS[i];
					if(src == 0) {
						src = state.defaultSrc;
					}
					if(dst == 0) {
						dst = state.defaultDst;
					}
					state.set(src, dst);
				}
			} else {
				if(!blendAll) {
					if(GLContextState.BLEND_STATE_IS != null) {
						for(GLContextState.BlendStateI state : GLContextState.BLEND_STATE_IS) {
							state.set(state.defaultSrc, state.defaultDst);
						}
					} else {
						GLContextState.blendFunc(GLContextState.BLEND_ALL_INTERNAL.defaultSrc, GLContextState.BLEND_ALL_INTERNAL.defaultDst);
					}
				}
			}
			//endregion
		}

		if(builder.isEnabled(DEPTH_MASK_SET)) {
			GLContextState.DEPTH_MASK.set(builder.getBoolean(DEPTH_MASK));
		} else {
			GLContextState.DEPTH_MASK.setToDefault();
		}

		boolean depthTest;
		if(builder.isEnabled(DEPTH_TEST_SET)) {
			depthTest = GLContextState.DEPTH_TEST.set(builder.getBoolean(DEPTH_TEST));
		} else {
			depthTest = GLContextState.DEPTH_TEST.setToDefault();
		}

		if(depthTest) {
			if(builder.isEnabled(DEPTH_FUNC_SET)) {
				GLContextState.DEPTH_FUNC.set(builder.depthFunc);
			} else {
				GLContextState.DEPTH_FUNC.setToDefault();
			}
		}

		if(builder.isEnabled(FACE_CULLING_SET)) {
			GLContextState.FACE_CULLING.set(builder.getBoolean(FACE_CULLING_SET));
		} else {
			GLContextState.FACE_CULLING.setToDefault();
		}
	}

	@Override
	public void applyState() {
		apply(this.state);
	}

	@Override
	public GLStateBuilder copyToBuilder() {
		return new GLStateBuilder(this.state);
	}

	@Override
	public BuiltGlState copy() {
		return this; // immutable
	}
}
