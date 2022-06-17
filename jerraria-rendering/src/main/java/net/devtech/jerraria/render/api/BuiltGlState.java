package net.devtech.jerraria.render.api;

import static org.lwjgl.opengl.GL11C.GL_LESS;

public interface BuiltGlState {
	BuiltGlState DEPTH = builder().depthTest(true).depthFunc(GL_LESS).depthMask(true).build();
	BuiltGlState EMPTY = builder().build();

	static GLStateBuilder builder() {
		return GLStateBuilder.builder();
	}

	/**
	 * Applies the GlState to the environment
	 */
	void applyState();

	GLStateBuilder copyToBuilder();

	BuiltGlState copy();
}
