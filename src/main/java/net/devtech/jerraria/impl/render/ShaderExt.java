package net.devtech.jerraria.impl.render;

import net.minecraft.client.gl.GlUniform;

public interface ShaderExt {
	void markDirty(GlUniform uniform);
}
