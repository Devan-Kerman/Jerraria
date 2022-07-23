package net.devtech.jerraria.mixin.impl;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;

@Mixin(Shader.class)
public interface ShaderAccess {
	@Accessor
	static Shader getActiveShader() {throw new UnsupportedOperationException();}

	@Accessor
	static int getActiveShaderId() {throw new UnsupportedOperationException();}

	@Accessor
	static void setActiveShader(Shader activeShader) {throw new UnsupportedOperationException();}

	@Accessor
	static void setActiveShaderId(int activeShaderId) {throw new UnsupportedOperationException();}

	@Mutable
	@Accessor
	void setLoadedAttributeIds(List<Integer> loadedAttributeIds);

	@Mutable
	@Accessor
	void setAttributeNames(List<String> attributeNames);

	@Mutable
	@Accessor
	void setSamplerNames(List<String> samplerNames);

	@Mutable
	@Accessor
	void setUniforms(List<GlUniform> uniforms);

	@Mutable
	@Accessor
	void setLoadedUniformIds(List<Integer> loadedUniformIds);

	@Mutable
	@Accessor
	void setLoadedUniforms(Map<String, GlUniform> loadedUniforms);

	@Mutable
	@Accessor
	void setBlendState(GlBlendState blendState);

	@Mutable
	@Accessor
	void setProgramId(int programId);

	@Accessor
	void setDirty(boolean dirty);

	@Accessor
	GlBlendState getBlendState();

	@Accessor
	List<String> getSamplerNames();

	@Accessor
	Map<String, Object> getSamplers();

	@Accessor
	List<GlUniform> getUniforms();
}
