package net.devtech.jerraria.render.internal.renderhandler;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.impl.RenderingEnvironment;

public class OpaqueRenderHandler implements RenderHandler {
	public static final BuiltGlState OPAQUE = RenderingEnvironment.DEFAULT_GL_STATE;

	@Override
	public void drawKeep(Shader<?> shader, BuiltGlState state) {
		shader.getShader().bindProgram();
		shader.getShader().drawKeep(state);
	}

	@Override
	public void drawInstancedKeep(Shader<?> shader, BuiltGlState state, int count) {
		shader.getShader().bindProgram();
		shader.getShader().drawInstancedKeep(state, count);
	}

	@Override
	public BuiltGlState defaultGlState() {
		return OPAQUE;
	}
}
