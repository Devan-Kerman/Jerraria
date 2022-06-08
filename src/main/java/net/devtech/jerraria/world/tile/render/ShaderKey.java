package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.renderhandler.OpaqueRenderHandler;
import net.devtech.jerraria.world.tile.render.ShaderSource.ShaderConfigurator;

public record ShaderKey<T extends Shader<?>>(
	ShaderConfigurator<? super T> config,
	T shader,
	SCopy copy,
	AutoBlockLayerInvalidation invalidation,
	DrawMethod primitive,
	BuiltGlState state
) {

	public ShaderKey(ShaderConfigurator<? super T> config, T shader) {
		this(
			config,
			shader,
			SCopy.PRESERVE_NEITHER,
			AutoBlockLayerInvalidation.ON_BLOCK_UPDATE,
			DrawMethod.TRIANGLE,
			OpaqueRenderHandler.OPAQUE
		);
	}

	public ShaderKey<T> withConfigurator(ShaderConfigurator<? super T> config) {
		return new ShaderKey<>(config, this.shader, this.copy, this.invalidation, this.primitive, this.state);
	}

	public <S extends Shader<?>> ShaderKey<S> withShader(S shader, ShaderConfigurator<? super S> config) {
		return new ShaderKey<>(config, shader, this.copy, this.invalidation, this.primitive, this.state);
	}

	public ShaderKey<T> withCopy(SCopy copy) {
		return new ShaderKey<>(this.config, this.shader, copy, this.invalidation, this.primitive, this.state);
	}

	public ShaderKey<T> withInvalidation(AutoBlockLayerInvalidation invalidation) {
		return new ShaderKey<>(this.config, this.shader, this.copy, invalidation, this.primitive, this.state);
	}

	public ShaderKey<T> withMethod(DrawMethod primitive) {
		return new ShaderKey<>(this.config, this.shader, this.copy, this.invalidation, primitive, this.state);
	}

	public ShaderKey<T> withState(BuiltGlState state) {
		return new ShaderKey<>(this.config, this.shader, this.copy, this.invalidation, this.primitive, state);
	}

}
