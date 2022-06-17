package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.internal.renderhandler.OpaqueRenderHandler;

public record ChunkShaderKey<T extends Shader<?>>(
	ChunkShaderConfigurator<? super T> config,
	T shader,
	SCopy copy,
	BuiltGlState state
) {

	public ChunkShaderKey(ChunkShaderConfigurator<? super T> config, T shader) {
		this(
			config,
			shader,
			SCopy.PRESERVE_NEITHER,
			OpaqueRenderHandler.OPAQUE
		);
	}

	public ChunkShaderKey<T> withConfigurator(ChunkShaderConfigurator<? super T> config) {
		return new ChunkShaderKey<>(config, this.shader, this.copy,  this.state);
	}

	public <S extends Shader<?>> ChunkShaderKey<S> withShader(S shader, ChunkShaderConfigurator<? super S> config) {
		return new ChunkShaderKey<>(config, shader, this.copy,  this.state);
	}

	public ChunkShaderKey<T> withCopy(SCopy copy) {
		return new ChunkShaderKey<>(this.config, this.shader, copy,  this.state);
	}

	public ChunkShaderKey<T> withState(BuiltGlState state) {
		return new ChunkShaderKey<>(this.config, this.shader, this.copy,  state);
	}
}
