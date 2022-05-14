package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;

public class ShaderManager {
	public static final List<Function<Id, String>> FRAG_SOURCES = new ArrayList<>(), VERT_SOURCES = new ArrayList<>();
	public static final List<Function<Id, ShaderPair>> SHADER_PROVIDERS = new ArrayList<>();
	private static final Function<Id, String> FRAG_SRC = findFirst(FRAG_SOURCES), VERT_SRC = findFirst(VERT_SOURCES);
	private static final Function<Id, ShaderPair> SHADER_PAIRS = findFirst(SHADER_PROVIDERS);
	public static final Map<Id, BareShader> SHADER_CACHE = new HashMap<>();

	public record ShaderPair(Id frag, Id vert) {}

	public static BareShader getBareShader(Id id, List<GlValue.Type<?>> vertex, List<GlValue.Type<?>> uniforms) {
		BareShader shader = SHADER_CACHE.get(id);
		if(shader == null) {
			SHADER_CACHE.put(id, shader = getShader(id, vertex, uniforms));
		} else {
			shader = new BareShader(shader, SCopy.PRESERVE_NEITHER);
		}
		return shader;
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

	public static void reloadShader(BareShader shader, Id id) {
		int fragmentShader = BareShader.createProgram(FRAG_SRC, GL_FRAGMENT_SHADER, id);
		int vertexShader = BareShader.createProgram(FRAG_SRC, GL_VERTEX_SHADER, id);
		int oldId = shader.id.glId;
		shader.id.glId = compileShader(fragmentShader, vertexShader);
		glDeleteProgram(oldId);
	}

	static int compileShader(int fragmentShader, int vertexShader) {
		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);
		if(glGetProgrami(program, GL_LINK_STATUS) == 0) {
			System.err.println("Error compiling shader!");
			System.err.println(glGetProgramInfoLog(program));
		}
		return program;
	}
}
