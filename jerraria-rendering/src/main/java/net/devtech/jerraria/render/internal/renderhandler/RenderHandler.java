package net.devtech.jerraria.render.internal.renderhandler;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.Shader;

public class RenderHandler {
	public static final RenderHandler INSTANCE = new RenderHandler();

	public void drawKeep(Shader<?> shader, BuiltGlState state) {
		shader.getShader().bindProgram();
		shader.getShader().drawKeep(state);
	}

	public void drawInstancedKeep(Shader<?> shader, BuiltGlState state, int count) {
		shader.getShader().bindProgram();
		shader.getShader().drawInstancedKeep(state, count);
	}

	public BuiltGlState defaultGlState() {
		return BuiltGlState.DEFAULT;
	}
}
