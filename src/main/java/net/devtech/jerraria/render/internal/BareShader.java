package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.*;
import java.lang.ref.Cleaner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import org.lwjgl.opengl.GL20;

/**
 * Un-abstracted view of a "shader object" (think VAO or UBO) thing.
 *  The shader object has its own VAO and its own UBO.
 */
public class BareShader {
	public static final Cleaner GL_CLEANUP = Cleaner.create();
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
			int fragmentShader = getOrCompileShader(fragSrc, fragmentShaders, uncompiled.frag, GL_FRAGMENT_SHADER);
			int vertexShader = getOrCompileShader(vertSrc, vertexShaders, uncompiled.vert, GL_VERTEX_SHADER);
			int program = glCreateProgram();
			glAttachShader(program, vertexShader);
			glAttachShader(program, fragmentShader);
			glLinkProgram(program);
			if(glGetProgrami(program, GL_LINK_STATUS) == 0) {
				System.err.println("Error compiling shader!");
				System.err.println(glGetProgramInfoLog(program));
			}

			//glUseProgram(program);
			VAO vertex = new VAO(uncompiled.vertexFields, program, uncompiled.id);
			UniformData uniform = new UniformData(uncompiled.uniformFields, program, uncompiled.id);
			BareShader shader = new BareShader(program, vertex, uniform);
			compiledShaders.put(uncompiled.id, shader);
		}
		fragmentShaders.values().forEach(GL20::glDeleteShader);
		vertexShaders.values().forEach(GL20::glDeleteShader);
		return compiledShaders;
	}

	private static int getOrCompileShader(Function<Id, String> src,
		Object2IntMap<Id> cache,
		Id sourceId, int type) {
		return cache.computeIfAbsent(sourceId, (Id id) -> {
			String source = src.apply(id);
			int glId = glCreateShader(type);
			glShaderSource(glId, source);
			glCompileShader(glId);
			return glId;
		});
	}

	private void setupDraw() {
		int id = this.glId;
		BareShader active = activeShader;
		if(active == null || active.glId != id) {
			glUseProgram(id);
			activeShader = this;
		}
		this.uniforms.upload();
	}

	public void draw(int primitive) {
		this.setupDraw();
		this.vao.bindAndDraw(primitive);
	}

	public void drawInstanced(int primitive, int count) {
		this.setupDraw();
		this.vao.bindAndDrawInstanced(primitive, count);
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

		public Uncompiled vert(DataType type, String name, String groupName) {
			this.vertexFields.put(name, new Field(type, name, groupName));
			return this;
		}

		public Uncompiled uniform(DataType type, String name, String groupName) {
			this.uniformFields.put(name, new Field(type, name, groupName));
			return this;
		}

		public Uncompiled type(GlValue.Loc type, DataType local, String name, String groupName) {
			if(type == GlValue.Loc.UNIFORM) {
				return uniform(local, name, groupName);
			} else {
				return vert(local, name, groupName);
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
