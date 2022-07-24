package net.devtech.jerraria.mixin.self;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.devtech.jerraria.mixin.impl.ShaderAccess;
import net.devtech.jerraria.render.internal.state.GLContextState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.GlProgramManager;

@Mixin(value = GLContextState.class, remap = false)
public class GLContextStateMixin {
	@Shadow @Mutable @Final static boolean FORCE;
	@Shadow @Mutable @Final static int defaultFBO;
	@Shadow @Final static GLContextState.BlendStateI BLEND_ALL_INTERNAL;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void setForce(CallbackInfo info) {
		FORCE = true;
	}

	@Redirect(method = {
		"bindFrameBuffer(I)V",
		"bindDrawFBO(I)V",
		"bindReadFBO(I)V"
	}, at = @At(value = "INVOKE", target = "glBindFramebuffer(II)V"))
	private static void bindFrameBuffer(int target, int id) {
		int default_ = defaultFBO;
		GlStateManager._glBindFramebuffer(target, id);
		defaultFBO = default_;
	}

	/**
	 * @author HalfOf2
	 * @reason update minecraft's stuff
	 */
	@Overwrite
	public static void bindProgram(int glId) {
		if(ShaderAccess.getActiveShaderId() != glId) {
			GlProgramManager.useProgram(glId);
			ShaderAccess.setActiveShaderId(glId);
			ShaderAccess.setActiveShader(null);
		}
	}

	@Overwrite
	public static void bindVAO(int vaoId) {
		GlStateManager._glBindVertexArray(vaoId);
	}

	@Overwrite
	public static void blendFunc(int src, int dst) {
		int dsrc = BLEND_ALL_INTERNAL.defaultSrc, ddst = BLEND_ALL_INTERNAL.defaultDst;
		GlStateManager._blendFunc(src, dst);
		BLEND_ALL_INTERNAL.defaultSrc = dsrc;
		BLEND_ALL_INTERNAL.defaultDst = ddst;
	}

	@Mixin(value = GLContextState.BoolState.class, remap = false)
	static class BoolStateMixin {
		@Shadow @Final
		BooleanConsumer binder;
		@Shadow boolean default_;

		@Overwrite
		public void set(boolean value) {
			boolean default_ = this.default_;
			this.binder.accept(value);
			this.default_ = default_;
		}
	}
}