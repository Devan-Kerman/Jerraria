package net.devtech.jerraria.render.api;

import static org.lwjgl.opengl.GL11C.GL_ALWAYS;

public interface BuiltGlState {
	BuiltGlState DEFAULT = GLStateBuilder.builder().depthTest(true).depthFunc(GL_ALWAYS).depthMask(true).build();

	void apply();

	GLStateBuilder copyToBuilder();
}
