package net.devtech.jerraria.render.api.impl;


import java.util.Objects;

import net.devtech.jerraria.render.api.BuiltGlState;

public interface RenderingEnvironment { // todo undo this in favor of a static class that pulls from PlatformInternal
	Thread RENDER_THREAD = Objects.requireNonNull(
		RenderingEnvironmentInternal.renderThread_,
		"RenderingEnvironmentInternal.renderThread_ not set!"
	);

	BuiltGlState DEFAULT_GL_STATE = Objects.requireNonNull(
		RenderingEnvironmentInternal.defaultState_,
		"RenderingEnvironmentInternal.defaultState_ not set!"
	);

	static void validateRenderThread(String operationName) {
		if(RENDER_THREAD != Thread.currentThread()) {
			throw new UnsupportedOperationException("Cannot " + operationName + " off render thread!");
		}
	}
}
