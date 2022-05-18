package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL31.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL31.glCompileShader;
import static org.lwjgl.opengl.GL31.glCreateShader;
import static org.lwjgl.opengl.GL31.glShaderSource;
import static org.lwjgl.opengl.GL31.glUseProgram;

import java.lang.ref.Cleaner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.element.AutoElementFamily;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.internal.element.Seq;
import net.devtech.jerraria.render.internal.element.ShapeStrat;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import org.lwjgl.opengl.GL20;

/**
 * Un-abstracted view of a "shader object" (think VAO or UBO) thing. The shader object has its own VAO and its own UBO.
 */
public class BareShader {
	public static final Cleaner GL_CLEANUP = Cleaner.create();
	public static BareShader activeShader;
	public final VAO vao;
	public final UniformData uniforms;
	public EBO ebo;
	public AutoStrat strategy = AutoStrat.TRIANGLE;
	int lastCopiedVertex;
	GlIdReference id;
	int currentGlId;

	public BareShader(int glId, VAO data, UniformData uniformData) {
		this.id = new GlIdReference();
		this.id.glId = glId;
		this.currentGlId = glId;
		this.vao = data;
		this.uniforms = uniformData;
	}

	public BareShader(BareShader shader, SCopy method) {
		this.id = shader.id;
		this.currentGlId = shader.currentGlId;
		this.vao = new VAO(shader.vao, method.preserveVertexData);
		this.uniforms = new UniformData(shader.uniforms, method.preserveUniforms);
		if(shader.ebo != null) {
			this.ebo = new EBO(shader.ebo);
			this.strategy = shader.strategy;
		}
	}

	public static Map<Id, BareShader> compileShaders(
		Function<Id, String> fragSrc,
		Function<Id, String> vertSrc,
		List<Uncompiled> shaders) {
		Object2IntMap<Id> fragmentShaders = new Object2IntOpenHashMap<>(), vertexShaders =
			                                                                   new Object2IntOpenHashMap<>();
		Map<Id, BareShader> compiledShaders = new HashMap<>();
		for(Uncompiled uncompiled : shaders) {
			int fragmentShader = getOrCompileShader(fragSrc, fragmentShaders, uncompiled.frag, GL_FRAGMENT_SHADER);
			int vertexShader = getOrCompileShader(vertSrc, vertexShaders, uncompiled.vert, GL_VERTEX_SHADER);
			int program = ShaderManager.compileShader(fragmentShader, vertexShader);
			try {
				VAO vertex = new VAO(uncompiled.vertexFields, program, uncompiled.id);
				UniformData uniform = new UniformData(uncompiled.uniformFields, program, uncompiled.id);
				BareShader shader = new BareShader(program, vertex, uniform);
				compiledShaders.put(uncompiled.id, shader);
			} catch(Throwable t) {
				ShaderValidationException rethrow = new ShaderValidationException(String.format(
					"Exception when validating %s [frag: %s] [vert: %s]",
					uncompiled.id,
					uncompiled.frag,
					uncompiled.vert
				), t);
				Validate.simplifyStackTrace(t, rethrow);
				throw Validate.rethrow(rethrow);
			}
		}
		fragmentShaders.values().forEach(GL20::glDeleteShader);
		vertexShaders.values().forEach(GL20::glDeleteShader);
		return compiledShaders;
	}

	static final class ShaderValidationException extends Exception {
		public ShaderValidationException(String message, Throwable cause) {
			super(message, cause);
		}

		public ShaderValidationException(String message) {
			super(message);
		}
	}

	public static int createProgram(Function<Id, String> src, int type, Id id) {
		String source = src.apply(id);
		int glId = glCreateShader(type);
		glShaderSource(glId, source);
		glCompileShader(glId);
		return glId;
	}

	public void draw() { // todo fully write EBO
		int mode = this.strategy.getDrawMethod().glId;
		int type = this.setupDraw(true);
		if(type == -1) {
			this.vao.drawArrays(mode);
		} else {
			this.vao.drawElements(mode, this.getVertexCount(), type);
		}
	}

	public void drawInstanced(int count) {
		int mode = this.strategy.getDrawMethod().glId;
		int type = this.setupDraw(true);
		if(type == -1) {
			this.vao.drawArraysInstanced(mode, count);
		} else {
			this.vao.drawElementsInstanced(mode, this.getVertexCount(), type, count);
		}
	}

	public int getVertexCount() {
		if(this.ebo == null) {
			return this.strategy.elementsForVertexData(this.vao.last.buffer.vertexCount);
		} else {
			return this.ebo.builder.getVertexCount();
		}
	}

	public void deleteVertexData() {
		this.vao.flush();
		this.lastCopiedVertex = 0;
		if(this.ebo != null) {
			this.ebo.clear();
			this.ebo = null;
		}
	}

	private static int getOrCompileShader(Function<Id, String> src, Object2IntMap<Id> cache, Id sourceId, int type) {
		return cache.computeIfAbsent(sourceId, (Id id) -> createProgram(src, type, id));
	}

	public void hotswapStrategy(AutoStrat strategy, boolean force) {
		// if strategy is not same
			// populate EBO with old strategy data
		AutoStrat current = this.strategy;
		if(current != strategy || force) {
			if(this.vao.last.buffer.vertexCount == this.lastCopiedVertex) {
				this.strategy = strategy;
				return;
			}

			if(current.getDrawMethod() != strategy.getDrawMethod() && current.getDrawMethod() != null && strategy.getDrawMethod() != null) {
				throw new UnsupportedOperationException("Cannot render one half of vertex data in " + current.getDrawMethod() + " and the other in " + strategy.getDrawMethod());
			}

			this.strategy = strategy;

			AutoElementFamily family = (AutoElementFamily) current;
			int count = this.vao.last.buffer.vertexCount;
			if(this.ebo == null) {
				int elements = current.elementsForVertexData(count);
				if(elements != 0) {
					this.ebo = new EBO(family.forCount(count), elements);
				} else {
					this.ebo = new EBO();
				}
			} else {
				int len = current.elementsForVertexData(count - this.lastCopiedVertex);
				if(len != 0) {
					int start = current.elementsForVertexData(this.lastCopiedVertex);
					this.ebo.append(family.forCount(count), start, len);
				}
			}
			this.lastCopiedVertex = count;
		}
	}

	private int setupDraw(boolean bindVao) {
		int id = this.id.glId;
		BareShader active = activeShader;
		if(active == null || active.currentGlId != id) {
			glUseProgram(id);
			activeShader = this;
		}
		if(id != this.currentGlId) {
			this.vao.markForReupload();
			this.uniforms.markForReupload();
			if(this.ebo != null) {
				this.ebo.markForReupload();
			}
			this.currentGlId = id;
		}

		this.uniforms.upload();
		if(bindVao) {
			this.vao.bind();
			if(this.ebo == null && this.strategy instanceof AutoElementFamily f && f.byte_ instanceof Seq) {
				return -1;
			} else if(this.ebo != null) {
				this.hotswapStrategy(this.strategy, true);
				this.ebo.bind();
				return this.ebo.currentType;
			} else {
				// custom strategy without hotswaps
				ShapeStrat strat = ((AutoElementFamily) this.strategy).forCount(this.vao.last.buffer.vertexCount);
				strat.bind();
				return strat.getType();
			}
		} else {
			return -1;
		}
	}

	public static final class GlIdReference {
		int glId;
	}

	public static final class Uncompiled {
		final Id id;
		final Id frag, vert;
		final Map<String, Field> vertexFields;
		final Map<String, Field> uniformFields;

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
				return this.uniform(local, name, groupName);
			} else {
				return this.vert(local, name, groupName);
			}
		}
	}

	public record Field(DataType type, String name, String groupName) {
		public Field(DataType type, String name) {
			this(type, name, null);
		}

		public String groupName(boolean isUniform) {
			String name = this.groupName;
			if(name != null || isUniform) {
				return name;
			} else {
				return "default_";
			}
		}
	}
}
