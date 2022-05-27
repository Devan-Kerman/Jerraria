package net.devtech.jerraria.render.api;

public interface BuiltGlState {
	BuiltGlState DEFAULT = GLStateBuilder.builder().build();
	void apply();

	GLStateBuilder copyToBuilder();
}
