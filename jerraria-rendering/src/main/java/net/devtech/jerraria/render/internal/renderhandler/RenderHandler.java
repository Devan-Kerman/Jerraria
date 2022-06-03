package net.devtech.jerraria.render.internal.renderhandler;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.Shader;

public interface RenderHandler {
	RenderHandler INSTANCE = new OpaqueRenderHandler();

	void drawKeep(Shader<?> shader, BuiltGlState state);

	void drawInstancedKeep(Shader<?> shader, BuiltGlState state, int count);

	BuiltGlState defaultGlState();
}
