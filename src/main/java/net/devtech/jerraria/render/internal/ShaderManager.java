package net.devtech.jerraria.render.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.api.GlValue;

public class ShaderManager {
	public static final List<Function<Id, String>> FRAG_SOURCES = new ArrayList<>(), VERT_SOURCES = new ArrayList<>();
	public static final List<Function<Id, ShaderPair>> SHADER_PROVIDERS = new ArrayList<>();
	private static final Function<Id, String> FRAG_SRC = findFirst(FRAG_SOURCES), VERT_SRC = findFirst(VERT_SOURCES);
	private static final Function<Id, ShaderPair> SHADER_PAIRS = findFirst(SHADER_PROVIDERS);
	private static final Map<Id, BareShader> SHADER_CACHE = new HashMap<>();

	public record ShaderPair(Id frag, Id vert) {}

	public static BareShader getBareShader(Id id, List<GlValue.Type<?>> vertex, List<GlValue.Type<?>> uniforms) {
		return SHADER_CACHE.computeIfAbsent(id, id2 -> getShader(id2, vertex, uniforms));
	}

	private static <T> Function<Id, T> findFirst(List<Function<Id, T>> list) {
		return id1 -> list.stream().map(f -> f.apply(id1)).filter(Objects::nonNull).findFirst().orElseThrow();
	}

	private static BareShader getShader(Id id, List<GlValue.Type<?>> vertex, List<GlValue.Type<?>> uniforms) {
		ShaderPair pair = SHADER_PAIRS.apply(id);
		var uncompiled = new BareShader.Uncompiled(id, pair.frag, pair.vert);
		for(GlValue.Type<?> type : vertex) {
			type.attach(uncompiled, GlValue.Loc.ATTRIBUTE);
		}
		for(GlValue.Type<?> uniform : uniforms) {
			uniform.attach(uncompiled, GlValue.Loc.UNIFORM);
		}
		return BareShader.compileShaders(FRAG_SRC, VERT_SRC, List.of(uncompiled)).get(id);
	}
}
