package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.GlData;

public abstract class GlValue<N extends ShaderStage> extends ShaderStage {
	GlData data;

	public interface Type<N extends ShaderStage> {
		N create();
	}
}
