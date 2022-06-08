package net.devtech.jerraria.render.api;

import static net.devtech.jerraria.render.api.GLStateBuilder.*;

import net.devtech.jerraria.render.internal.state.GLContextState;

record GlStateStackImpl(GLStateBuilder original, GLStateBuilder current) implements GlStateStack {
	GlStateStackImpl {
		applyDefaults(current);
	}

	GlStateStackImpl(GLStateBuilder current) {
		this(recordState(current.set), current);
	}

	public static void applyDefaults(GLStateBuilder current) {
		if(current.isEnabled(BLEND_SET)) {
			GLContextState.BLEND.setAndDefault(current.getBoolean(BLEND));
		}

		if(current.isEnabled(BLEND_EQ_SET)) {
			GLContextState.BLEND_EQUATION.setAndDefault(current.blendEquation);
		}

		if(current.isEnabled(BLEND_ALL_SET)) {
			GLContextState.blendFunc(current.blendSrc, current.blendDst);
			GLContextState.BLEND_ALL_INTERNAL.defaultSrc = current.blendSrc;
			GLContextState.BLEND_ALL_INTERNAL.defaultDst = current.blendDst;
		}

		if(current.isEnabled(BLEND_I_SET)) {
			int[] srcs = current.blendISrc, dsts = current.blendIDst;
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
				state.defaultSrc = src;
				state.defaultDst = dst;
			}
		}

		if(current.isEnabled(DEPTH_MASK_SET)) {
			GLContextState.DEPTH_MASK.setAndDefault(current.getBoolean(DEPTH_MASK));
		}

		if(current.isEnabled(DEPTH_TEST_SET)) {
			GLContextState.DEPTH_TEST.setAndDefault(current.getBoolean(DEPTH_TEST));
		}

		if(current.isEnabled(DEPTH_FUNC_SET)) {
			GLContextState.DEPTH_FUNC.setAndDefault(current.depthFunc);
		}

		if(current.isEnabled(FACE_CULLING_SET)) {
			GLContextState.DEPTH_TEST.setAndDefault(current.getBoolean(FACE_CULLING));
		}
	}

	public static GLStateBuilder recordState(int enabled) {
		GLStateBuilder builder = new GLStateBuilder();
		builder.set = enabled;
		if(builder.isEnabled(BLEND_SET)) {
			builder.blend(GLContextState.BLEND.getDefault());
		}

		if(builder.isEnabled(BLEND_EQ_SET)) {
			builder.blendEq(GLContextState.BLEND_EQUATION.getDefault());
		}

		if(builder.isEnabled(BLEND_ALL_SET) || builder.isEnabled(BLEND_I_SET)) {
			if(GLContextState.BLEND_STATE_IS != null) {
				int srcAll = -1, dstAll = -1;
				for(GLContextState.BlendStateI state : GLContextState.BLEND_STATE_IS) {
					int src = state.getDefaultSrc();
					if(srcAll == -1) {
						srcAll = src;
					} else if(srcAll != src) {
						srcAll = -2;
						break;
					}

					int dst = state.getDefaultDst();
					if(dstAll == -1) {
						dstAll = dst;
					} else if(dstAll != dst) {
						srcAll = -2;
						break;
					}
				}

				if(srcAll == -2) { // no common blend state
					int length = GLContextState.BLEND_STATE_IS.length;
					int[] src = new int[length], dst = new int[length];
					for(int i = 0; i < length; i++) {
						GLContextState.BlendStateI state = GLContextState.BLEND_STATE_IS[i];
						src[i] = state.defaultSrc;
						dst[i] = state.defaultDst;
					}
					builder.unsetBlendFunc();
					builder.dstBlendFuncs(dst);
					builder.srcBlendFuncs(src);
				} else {
					builder.unsetBlendFuncI();
					builder.blendFunc(srcAll, dstAll);
				}
			} else {
				builder.unsetBlendFuncI();
				GLContextState.BlendStateI internal = GLContextState.BLEND_ALL_INTERNAL;
				builder.blendFunc(internal.defaultSrc, internal.defaultDst);
			}
		}

		if(builder.isEnabled(DEPTH_FUNC_SET)) {
			builder.depthFunc(GLContextState.DEPTH_FUNC.getDefault());
		}

		if(builder.isEnabled(DEPTH_TEST_SET)) {
			builder.depthTest(GLContextState.DEPTH_TEST.getDefault());
		}

		if(builder.isEnabled(DEPTH_MASK_SET)) {
			builder.depthMask(GLContextState.DEPTH_MASK.getDefault());
		}

		if(builder.isEnabled(FACE_CULLING_SET)) {
			builder.faceCulling(GLContextState.FACE_CULLING.getDefault());
		}
		return builder;
	}

	@Override
	public void forceReapply() {
		applyDefaults(this.current);
	}

	@Override
	public GLStateBuilder copyToBuilder() {
		return new GLStateBuilder(this.current);
	}

	@Override
	public void close() {
		applyDefaults(this.original);
	}

	@Override
	public GlStateStack copy() {
		return this; // immutable
	}
}
