package net.devtech.jerraria.render.api.impl;


import static org.lwjgl.opengl.GL11C.GL_ALWAYS;

import java.util.Objects;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GLStateBuilder;

public interface PlatformProvider {
	BuiltGlState TWO_DIMENSIONAL = GLStateBuilder.builder().depthTest(true).depthFunc(GL_ALWAYS).depthMask(true).build();

	PlatformProvider DEFAULT = new DefaultPlatformProvider(
		PlatformInternal.renderThread_,
		TWO_DIMENSIONAL
	);

	Thread renderThread();

	BuiltGlState defaultGlState();

	default void validateRenderThread(String operationName) {
		if(this.renderThread() != Thread.currentThread()) {
			throw new UnsupportedOperationException("Cannot " + operationName + " off render thread!");
		}
	}

	record DefaultPlatformProvider(Thread renderThread, BuiltGlState defaultGlState) implements PlatformProvider {
		public DefaultPlatformProvider {
			Objects.requireNonNull(renderThread, "renderThread");
			Objects.requireNonNull(defaultGlState, "defaultGlState");
		}
	}
}
