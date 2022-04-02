package net.devtech.jerraria.client.render.internal;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.client.render.api.GlValue;
import net.devtech.jerraria.client.render.api.SCopy;
import org.lwjgl.opengl.GL20;

public class BareShader {
	public static BareShader activeShader;
	public final int glId;
	public final VAO vao;
	public final UniformData uniforms;

	public BareShader(int id, VAO data, UniformData uniformData) {
		this.glId = id;
		this.vao = data;
		this.uniforms = uniformData;
	}

	public BareShader(BareShader shader, SCopy method) {
		this.glId = shader.glId;
		this.vao = new VAO(shader.vao, method.preserveVertexData);
		this.uniforms = new UniformData(shader.uniforms, method.preserveUniforms);
	}

	public static Map<Id, BareShader> compileShaders(Function<Id, String> fragSrc, Function<Id, String> vertSrc, List<Uncompiled> shaders) {
		Object2IntMap<Id> fragmentShaders = new Object2IntOpenHashMap<>(), vertexShaders = new Object2IntOpenHashMap<>();
		Map<Id, BareShader> compiledShaders = new HashMap<>();
		for(Uncompiled uncompiled : shaders) {
			int fragmentShader = getOrCompileShader(fragSrc, fragmentShaders, uncompiled, GL_FRAGMENT_SHADER);
			int vertexShader = getOrCompileShader(vertSrc, vertexShaders, uncompiled, GL_VERTEX_SHADER);
			int program = glCreateProgram();
			glAttachShader(program, vertexShader);
			glAttachShader(program, fragmentShader);
			glLinkProgram(program);
			if(glGetProgrami(program, GL_LINK_STATUS) == 0) {
				System.err.println("Error compiling shader!");
				System.err.println(glGetProgramInfoLog(program));
			}

			VAO vertex = new VAO(uncompiled.vertexFields, program);
			UniformData uniform = new UniformData(uncompiled.uniformFields, program);
			BareShader shader = new BareShader(program, vertex, uniform);
			compiledShaders.put(uncompiled.id, shader);
		}
		fragmentShaders.values().forEach(GL20::glDeleteShader);
		vertexShaders.values().forEach(GL20::glDeleteShader);
		return compiledShaders;
	}

	private static int getOrCompileShader(Function<Id, String> src,
		Object2IntMap<Id> cache,
		Uncompiled shader, int type) {
		return cache.computeIfAbsent(shader.vert, (Id id) -> {
			String source = src.apply(id);
			int vertexShader = glCreateShader(type);
			glShaderSource(vertexShader, source);
			glCompileShader(vertexShader);
			return vertexShader;
		});
	}

	public void draw(int primitive) {
		glUseProgram(this.glId);
		this.uniforms.bind(activeShader != this);
		activeShader = this;
		this.vao.bindAndDraw(primitive);
	}

	public static final class Uncompiled {
		final Id id;
		final Id frag, vert;
		final Map<String, Field> vertexFields;
		final Map<String, Field> uniformFields;

		public Uncompiled(Id id, Id frag, Id vert, Map<String, Field> attributes, Map<String, Field> uniforms) {
			this.id = id;
			this.frag = frag;
			this.vert = vert;
			this.vertexFields = attributes;
			this.uniformFields = uniforms;
		}

		public Uncompiled(Id id, Id frag, Id vert) {
			this.id = id;
			this.frag = frag;
			this.vert = vert;
			this.vertexFields = new HashMap<>();
			this.uniformFields = new HashMap<>();
		}

		public Uncompiled vert(DataType type, String name) {
			this.vertexFields.put(name, new Field(type, name));
			return this;
		}

		public Uncompiled uniform(DataType type, String name) {
			this.uniformFields.put(name, new Field(type, name));
			return this;
		}

		public Uncompiled uniform(DataType type, String name, String groupName) {
			this.uniformFields.put(name, new Field(type, name, groupName));
			return this;
		}

		public Uncompiled type(GlValue.Loc type, DataType local, String name) {
			if(type == GlValue.Loc.UNIFORM) {
				return uniform(local, name);
			} else {
				return vert(local, name);
			}
		}
	}

	public record Field(DataType type, String name, String groupName) {
		public Field(DataType type, String name) {
			this(type, name, null);
		}

		public String groupName(boolean isUniform) {
			String name = this.groupName;
			if(name != null) {
				return name;
			} else if(isUniform) {
				return "default";
			} else {
				return this.name + "_";
			}
		}
	}
}
