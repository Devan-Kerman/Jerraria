package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.GlData;

public abstract class ShaderStage {
	public interface Universal {
	}

	ShaderStage next;
	GlData data;
}
