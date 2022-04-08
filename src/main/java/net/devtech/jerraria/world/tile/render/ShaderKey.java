package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.render.api.Primitive;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.world.tile.render.ShaderSource.ShaderConfigurator;

public record ShaderKey<T extends Shader<?>>(Id id,
                                             ShaderConfigurator<? super T> config,
                                             T shader,
                                             SCopy copy,
                                             AutoBlockLayerInvalidation invalidation,
                                             Primitive primitive) {
	public ShaderKey(
		Id id,
		ShaderConfigurator<? super T> config,
		T shader,
		AutoBlockLayerInvalidation invalidation,
		Primitive primitive) {
		this(id, config, shader, SCopy.PRESERVE_NEITHER, invalidation, primitive);
	}

	public ShaderKey(
		Id id, ShaderConfigurator<? super T> config, T shader, SCopy copy, AutoBlockLayerInvalidation invalidation) {
		this(id, config, shader, copy, invalidation, Primitive.TRIANGLE);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, SCopy copy, Primitive primitive) {
		this(id, config, shader, copy, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE, primitive);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, SCopy copy) {
		this(id, config, shader, copy, Primitive.TRIANGLE);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, Primitive primitive) {
		this(id, config, shader, SCopy.PRESERVE_NEITHER, primitive);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader, AutoBlockLayerInvalidation invalidation) {
		this(id, config, shader, SCopy.PRESERVE_NEITHER, invalidation);
	}

	public ShaderKey(Id id, ShaderConfigurator<? super T> config, T shader) {
		this(id, config, shader, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, SCopy copy, AutoBlockLayerInvalidation invalidation, Primitive primitive) {
		return new ShaderKey<T>(id, shader, shader, copy, invalidation, primitive);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, AutoBlockLayerInvalidation invalidation, Primitive primitive) {
		return key(id, shader, SCopy.PRESERVE_NEITHER, invalidation, primitive);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, SCopy copy, AutoBlockLayerInvalidation invalidation) {
		return key(id, shader, copy, invalidation, Primitive.TRIANGLE);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, SCopy copy, Primitive prim) {
		return key(id, shader, copy, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE, prim);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(Id id, T shader, SCopy copy) {
		return key(id, shader, copy, Primitive.TRIANGLE);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, Primitive primitive) {
		return key(id, shader, SCopy.PRESERVE_NEITHER, primitive);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(
		Id id, T shader, AutoBlockLayerInvalidation invalidation) {
		return key(id, shader, SCopy.PRESERVE_NEITHER, invalidation);
	}

	public static <T extends Shader<?> & ShaderConfigurator<? super T>> ShaderKey<T> key(Id id, T shader) {
		return key(id, shader, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE);
	}
}
