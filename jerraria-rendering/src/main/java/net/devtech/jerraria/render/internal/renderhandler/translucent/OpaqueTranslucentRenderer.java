package net.devtech.jerraria.render.internal.renderhandler.translucent;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.renderhandler.InternalTranslucencyRenderer;
import net.devtech.jerraria.render.internal.renderhandler.OpaqueRenderHandler;
import net.devtech.jerraria.util.Id;

public class OpaqueTranslucentRenderer extends OpaqueRenderHandler implements InternalTranslucencyRenderer {
	@Override
	public <S extends TranslucentShader<?>> S create(
		Id id, Copier<S> copier, Initializer<S> initializer) {
		return Shader.create(
			id,
			(old, method) -> copier.copy(old, method, TranslucentShaderType.SOLID),
			(builder, context) -> initializer.create(builder, context, TranslucentShaderType.SOLID)
		);
	}

	@Override
	public void renderStart() {

	}

	@Override
	public void renderResolve() {

	}

	@Override
	public void frameSize(int width, int height) {

	}
}
