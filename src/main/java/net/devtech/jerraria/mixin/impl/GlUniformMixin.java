package net.devtech.jerraria.mixin.impl;

import net.devtech.jerraria.impl.render.ShaderExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;

@Mixin(GlUniform.class)
public class GlUniformMixin {
	@Shadow @Final private GlShader program;

	@Shadow @Final private String name;

	@Inject(method = "markStateDirty", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlShader;markUniformsDirty()V"))
	public void enhancedContext(CallbackInfo ci) {
		if(this.program instanceof ShaderExt a) {
			a.markDirty((GlUniform) (Object) this);
		}
	}
}
