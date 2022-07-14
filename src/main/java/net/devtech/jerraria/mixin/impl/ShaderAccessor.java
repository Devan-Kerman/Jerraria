package net.devtech.jerraria.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.Shader;

@Mixin(Shader.class)
public interface ShaderAccessor {
	@Accessor
	static Shader getActiveShader() {throw new UnsupportedOperationException();}

	@Accessor
	static int getActiveShaderId() {throw new UnsupportedOperationException();}

	@Accessor
	static void setActiveShader(Shader activeShader) {throw new UnsupportedOperationException();}

	@Accessor
	static void setActiveShaderId(int activeShaderId) {throw new UnsupportedOperationException();}
}
