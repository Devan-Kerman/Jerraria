package net.devtech.jerraria.render.api;

public interface BuiltGlState {

	void apply();

	GLStateBuilder copyToBuilder();
}
