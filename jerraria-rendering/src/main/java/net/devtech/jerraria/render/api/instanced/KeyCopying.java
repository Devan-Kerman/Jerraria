package net.devtech.jerraria.render.api.instanced;

import java.util.List;
import java.util.function.Function;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderBuffer;

public final class KeyCopying {

	private KeyCopying() {}

	public static <T, U extends GlValue<?> & GlValue.Uniform & GlValue.Copiable> void ssbo(
		InstanceKey<T> from,
		InstanceKey<T> to,
		Function<T, ShaderBuffer<U>> extract) {
		copy(from, to, i -> i.ssbo(extract));
	}

	public static <T, U extends GlValue<?> & GlValue.Uniform & GlValue.Copiable> void uboL(
		InstanceKey<T> from,
		InstanceKey<T> to,
		Function<T, List<U>> extract) {
		copy(from, to, i -> i.uboL(extract));
	}

	public static <T, U extends GlValue<?> & GlValue.Uniform & GlValue.Copiable> void uboA(
		InstanceKey<T> from,
		InstanceKey<T> to,
		Function<T, U[]> extract) {
		copy(from, to, i -> i.uboA(extract));
	}

	public static <T, U extends GlValue<?> & GlValue.Uniform & GlValue.Copiable> void copy(
		InstanceKey<T> from,
		InstanceKey<T> to,
		Function<InstanceKey<T>, U> extract) {
		U fromBuf = extract.apply(from);
		U toBuf = extract.apply(to);
		Shader.copyUniform(fromBuf, toBuf);
	}
}
