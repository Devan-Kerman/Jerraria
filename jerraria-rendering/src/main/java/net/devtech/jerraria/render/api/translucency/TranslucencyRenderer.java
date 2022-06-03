package net.devtech.jerraria.render.api.translucency;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.util.Id;

public interface TranslucencyRenderer {
	<S extends TranslucentShader<?>> S create(Id id, Copier<S> copier, Initializer<S> initializer);

	interface Copier<T extends Shader<?>> {
		T copy(T old, SCopy method, TranslucentShaderType type);
	}

	interface Initializer<T extends Shader<?>> {
		T create(VFBuilder<End> builder, Object context, TranslucentShaderType type);
	}
}
