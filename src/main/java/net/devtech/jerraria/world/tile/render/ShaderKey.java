package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.world.tile.render.ShaderSource.ShaderConfigurator;

public record ShaderKey<T extends Shader<?>>(Id id,
                                             ShaderConfigurator<? super T> config,
                                             T shader,
                                             SCopy copy,
                                             AutoBlockLayerInvalidation invalidation,
                                             DrawMethod primitive) {
	public ShaderKey(
		Id id,
		ShaderConfigurator<? super T> config,
		T shader,
		AutoBlockLayerInvalidation invalidation,
		DrawMethod primitive) {
		this(id, config, shader, SCopy.PRESERVE_NEITHER, invalidation, primitive);
	}

	public ShaderKey(
		Id id, ShaderConfigurator<? super T> config, T shader, SCopy copy, AutoBlockLayerInvalidation invalidation) {
		this(id, config, shader, copy, invalidation, DrawMethod.TRIANGLE);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, SCopy copy, DrawMethod primitive) {
		this(id, config, shader, copy, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE, primitive);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, SCopy copy) {
		this(id, config, shader, copy, DrawMethod.TRIANGLE);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, DrawMethod primitive) {
		this(id, config, shader, SCopy.PRESERVE_NEITHER, primitive);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, AutoBlockLayerInvalidation invalidation) {
		this(id, config, shader, SCopy.PRESERVE_NEITHER, invalidation);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader) {
		this(id, config, shader, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, SCopy copy, AutoBlockLayerInvalidation invalidation, DrawMethod drawMethod) {
		return new ShaderKey<T>(id, shader, shader, copy, invalidation, drawMethod);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, AutoBlockLayerInvalidation invalidation, DrawMethod drawMethod) {
		return key(id, shader, SCopy.PRESERVE_NEITHER, invalidation, drawMethod);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, SCopy copy, AutoBlockLayerInvalidation invalidation) {
		return key(id, shader, copy, invalidation, DrawMethod.TRIANGLE);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, SCopy copy, DrawMethod prim) {
		return key(id, shader, copy, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE, prim);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(Id id, T shader, SCopy copy) {
		return key(id, shader, copy, DrawMethod.TRIANGLE);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, DrawMethod drawMethod) {
		return key(id, shader, SCopy.PRESERVE_NEITHER, drawMethod);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, AutoBlockLayerInvalidation invalidation) {
		return key(id, shader, SCopy.PRESERVE_NEITHER, invalidation);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(Id id, T shader) {
		return key(id, shader, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE);
	}
}
