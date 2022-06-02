package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.api.impl.PlatformProvider;

public interface BuiltGlState {
	BuiltGlState DEFAULT = PlatformProvider.DEFAULT.defaultGlState();

	void apply();

	GLStateBuilder copyToBuilder();
}
