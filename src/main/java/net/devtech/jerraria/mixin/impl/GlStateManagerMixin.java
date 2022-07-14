package net.devtech.jerraria.mixin.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import net.devtech.jerraria.render.internal.state.GLContextState;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
	@Inject(method = "_glBindFramebuffer", at = @At("RETURN"))
	static void bindFramebuffer(int target, int framebuffer, CallbackInfo ci) {
		GLContextState.setDefaultFrameBuffer(framebuffer);
	}

	@Inject(method = "_depthFunc", at = @At("RETURN"))
	static void depthFunc(int func, CallbackInfo ci) {
		GLContextState.DEPTH_FUNC.setDefault(func);
	}

	@Inject(method = "_depthMask", at = @At("RETURN"))
	static void depthMask(boolean mask, CallbackInfo ci) {
		GLContextState.DEPTH_MASK.setDefault(mask);
	}

	@Inject(method = "_enableDepthTest", at = @At("RETURN"), remap = false)
	static void depthTest0(CallbackInfo ci) {
		GLContextState.DEPTH_TEST.setDefault(true);
	}

	@Inject(method = "_disableDepthTest", at = @At("RETURN"), remap = false)
	static void depthTest1(CallbackInfo ci) {
		GLContextState.DEPTH_TEST.setDefault(false);
	}

	@Inject(method = "_enableBlend", at = @At("RETURN"), remap = false)
	static void blend0(CallbackInfo ci) {
		GLContextState.BLEND.setDefault(true);
	}

	@Inject(method = "_disableBlend", at = @At("RETURN"), remap = false)
	static void blend1(CallbackInfo ci) {
		GLContextState.BLEND.setDefault(false);
	}

	@Inject(method = "_enableCull", at = @At("RETURN"), remap = false)
	static void cull0(CallbackInfo ci) {
		GLContextState.FACE_CULLING.setDefault(true);
	}

	@Inject(method = "_disableCull", at = @At("RETURN"), remap = false)
	static void cull1(CallbackInfo ci) {
		GLContextState.FACE_CULLING.setDefault(false);
	}

	@Inject(method = "_blendEquation", at = @At("RETURN"))
	static void blendEq(int mode, CallbackInfo ci) {
		GLContextState.BLEND_EQUATION.setDefault(mode);
	}

	@Inject(method = "_blendFunc", at = @At("RETURN"))
	static void blendFunc(int srcFactor, int dstFactor, CallbackInfo ci) {
		GLContextState.BLEND_ALL_INTERNAL.defaultSrc = srcFactor;
		GLContextState.BLEND_ALL_INTERNAL.defaultDst = dstFactor;
	}
}
