package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.state.GLContextState;

record BuiltGlStateImpl(GLStateBuilder copy) implements BuiltGlState {
	@Override
	public void apply() {
		boolean blend;
		if(this.copy.isEnabled(GLStateBuilder.BLEND_SET)) {
			blend = GLContextState.BLEND.set(this.copy.getBoolean(GLStateBuilder.BLEND));
		} else {
			blend = GLContextState.BLEND.setToDefault();
		}

		if(blend) {
			if(this.copy.isEnabled(GLStateBuilder.BLEND_EQ_SET)) {
				GLContextState.BLEND_EQUATION.set(this.copy.blendEquation);
			} else {
				GLContextState.BLEND_EQUATION.setToDefault();
			}

			if(this.copy.isEnabled(GLStateBuilder.BLEND_ALL_SET)) {
				GLContextState.blendFunc(this.copy.blendSrc, this.copy.blendDst);
			} else {
				GLContextState.blendFunc(GLContextState.BLEND_ALL_INTERNAL.defaultSrc, GLContextState.BLEND_ALL_INTERNAL.defaultDst);
			}

			if(this.copy.isEnabled(GLStateBuilder.BLEND_I_SET)) {
				int[] srcs = this.copy.blendISrc, dsts = this.copy.blendIDst;
				for(int i = 0; i < Math.max(srcs.length, dsts.length); i++) {
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
				GLContextState.blendFunc(GLContextState.BLEND_ALL_INTERNAL.defaultSrc, GLContextState.BLEND_ALL_INTERNAL.defaultDst);
			}
		}


		if(this.copy.isEnabled(GLStateBuilder.DEPTH_MASK_SET)) {
			GLContextState.DEPTH_MASK.set(this.copy.getBoolean(GLStateBuilder.DEPTH_MASK));
		} else {
			GLContextState.DEPTH_MASK.setToDefault();
		}

		boolean depthTest;
		if(this.copy.isEnabled(GLStateBuilder.DEPTH_TEST_SET)) {
			depthTest = GLContextState.DEPTH_TEST.set(this.copy.getBoolean(GLStateBuilder.DEPTH_TEST));
		} else {
			depthTest = GLContextState.DEPTH_TEST.setToDefault();
		}

		if(depthTest) {
			if(this.copy.isEnabled(GLStateBuilder.DEPTH_FUNC_SET)) {
				GLContextState.DEPTH_FUNC.set(this.copy.depthFunc);
			} else {
				GLContextState.DEPTH_FUNC.setToDefault();
			}
		}
	}

	@Override
	public GLStateBuilder copyToBuilder() {
		return new GLStateBuilder(this.copy);
	}
}
