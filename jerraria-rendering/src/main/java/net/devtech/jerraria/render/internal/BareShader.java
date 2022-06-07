package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL31.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL31.glCompileShader;
import static org.lwjgl.opengl.GL31.glCreateShader;
import static org.lwjgl.opengl.GL31.glShaderSource;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.element.AutoElementFamily;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.internal.element.Seq;
import net.devtech.jerraria.render.internal.element.ShapeStrat;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20;

/**
 * Un-abstracted view of a "shader object" (think VAO or UBO) thing. The shader object has its own VAO and its own UBO.
 */
public class BareShader implements AutoCloseable {
	public static final boolean IN_DEV = Boolean.getBoolean("jerraria.dev");
	public static final Cleaner GL_CLEANUP = Cleaner.create();
	public final AtomicInteger uniqueIdCounter;
	public final int uniqueId;
	public final Id srcId;
	public final VertexData vao;
	public final UniformData uniforms;
	public final ReclamationManager manager;
	@Nullable public final FragmentOutputData outputs;
	@Nullable public EBO ebo;
	public AutoStrat strategy = AutoStrat.TRIANGLE;
	public GlIdReference id;
	int lastCopiedVertex;
	int currentGlId;

	public BareShader(Id id, int glId, VertexData data, UniformData uniformData, @Nullable FragmentOutputData output) {
		this.id = new GlIdReference();
		this.id.glId = glId;
		this.currentGlId = glId;
		this.vao = data;
		this.uniforms = uniformData;
		this.outputs = output;
		this.srcId = id;
		this.uniqueIdCounter = new AtomicInteger();
		this.uniqueId = this.uniqueIdCounter.getAndIncrement();
		this.manager = new ReclamationManager(null);
	}

	public BareShader(BareShader shader, SCopy method) { // todo keep track of initialization stacktrace
		this.srcId = shader.srcId;
		this.id = shader.id;
		this.currentGlId = shader.currentGlId;
		this.vao = new VertexData(shader.vao, method.preserveVertexData);
		this.uniforms = new UniformData(shader.uniforms, method.preserveUniforms);
		if(shader.ebo != null) {
			this.ebo = new EBO(shader.ebo);
		}
		this.strategy = shader.strategy;
		this.lastCopiedVertex = shader.lastCopiedVertex;
		if(shader.outputs != null) {
			this.outputs = new FragmentOutputData(shader.outputs);
		} else {
			this.outputs = null;
		}
		this.uniqueIdCounter = shader.uniqueIdCounter;
		this.uniqueId = this.uniqueIdCounter.getAndIncrement();

		StackTraceElement[] init = IN_DEV ? new Throwable().getStackTrace() : null;
		ReclamationManager manager = new ReclamationManager(init);
		this.manager = manager;
		GL_CLEANUP.register(this, manager);
	}

	public static Map<Id, BareShader> compileShaders(
		SourceProvider fragSrc, SourceProvider vertSrc, Map<String, Object> initialArgs, List<Uncompiled> shaders) {
		Object2IntMap<Id> fragmentShaders = new Object2IntOpenHashMap<>(), vertexShaders =
			                                                                   new Object2IntOpenHashMap<>();
		Map<Id, BareShader> compiledShaders = new HashMap<>();
		for(Uncompiled uncompiled : shaders) {
			ShaderPreprocessor preprocessor = new ShaderPreprocessor(ShaderManager.LIB_SRC);
			preprocessor.getIncludeParameters().putAll(initialArgs);
			try {
				int fragmentShader = getOrCompileShader(fragSrc,
					preprocessor,
					fragmentShaders,
					uncompiled.frag,
					GL_FRAGMENT_SHADER
				);
				int vertexShader = getOrCompileShader(vertSrc,
					preprocessor,
					vertexShaders,
					uncompiled.vert,
					GL_VERTEX_SHADER
				);
				int program = ShaderManager.compileShader(fragmentShader, vertexShader);
				VertexData vertex = new VertexData(uncompiled.vertexFields, program);
				UniformData uniform = new UniformData(uncompiled.uniformFields, program, uncompiled.id);
				FragmentOutputData output;
				if(uncompiled.outputFields.size() > 1) {
					GLSLParserValidation.validateFragShader(fragmentShader, uncompiled);
				}
				if(!uncompiled.outputFields.isEmpty()) {
					output = new FragmentOutputData(uncompiled.outputFields, program);
				} else {
					output = null;
				}
				BareShader shader = new BareShader(uncompiled.id, program, vertex, uniform, output);
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

	@Override
	public void close() throws Exception {
		if(!this.manager.isInvalid) {
			this.manager.isInvalid = true;
			this.uniforms.close();
			this.vao.close();
			if(this.outputs != null) {
				this.outputs.close();
			}
			if(this.ebo != null) {
				this.ebo.close();
			}
		}
	}

	public static int createProgram(SourceProvider src, ShaderPreprocessor initial, int type, Id id) {
		Pair<ShaderPreprocessor, String> source = src.getSource(initial, id);
		List<String> lines = new ArrayList<>();
		source.left().insert(source.value(), lines);
		int glId = glCreateShader(type);
		String[] strings = lines.toArray(String[]::new);
		glShaderSource(glId, strings);
		glCompileShader(glId);
		return glId;
	}

	/**
	 * exec {@link #bindProgram()} first
	 */
	public void drawKeep(BuiltGlState state) {
		this.validateState();
		state.apply();
		int mode = this.strategy.getDrawMethod().glId;
		int type = this.setupDraw(true);
		if(type == -1) {
			this.vao.drawArrays(mode);
		} else {
			this.vao.drawElements(mode, this.getVertexCount(), type);
		}
	}

	/**
	 * exec {@link #bindProgram()} first
	 */
	public void drawInstancedKeep(BuiltGlState state, int count) {
		this.validateState();
		state.apply();
		int mode = this.strategy.getDrawMethod().glId;
		int type = this.setupDraw(true);
		if(type == -1) {
			this.vao.drawArraysInstanced(mode, count);
		} else {
			this.vao.drawElementsInstanced(mode, this.getVertexCount(), type, count);
		}
	}

	public int getVertexCount() {
		this.validateState();
		if(this.ebo == null) {
			return this.strategy.elementsForVertexData(this.vao.last.getBuilder().getVertexCount());
		} else {
			return this.ebo.builder.getElementCount();
		}
	}

	public void deleteVertexData() {
		this.validateState();
		this.vao.flush();
		this.lastCopiedVertex = 0;
		this.ebo = null;
	}

	public void hotswapStrategy(AutoStrat strategy, boolean force) {
		this.validateState();
		AutoStrat current = this.strategy;
		if(current != strategy || force) {
			if(this.vao.last.getBuilder().getVertexCount() == this.lastCopiedVertex) {
				this.strategy = strategy;
				return;
			}

			if(current.getDrawMethod() != strategy.getDrawMethod() && current.getDrawMethod() != null && strategy.getDrawMethod() != null) {
				throw new UnsupportedOperationException("Cannot render one half of vertex data in " + current.getDrawMethod() + " and the other in " + strategy.getDrawMethod());
			}

			this.strategy = strategy;

			AutoElementFamily family = (AutoElementFamily) current;
			int count = this.vao.last.getBuilder().getVertexCount();
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

	public void bindProgram() {
		this.validateState();
		int id = this.id.glId;
		if(id != this.currentGlId) {
			this.uniforms.markForRebind();
			this.currentGlId = id;
		}
		GLContextState.bindProgram(id);
		if(this.outputs != null) {
			this.outputs.bind();
		} else {
			GLContextState.bindDefaultFrameBuffer();
		}
	}

	public int setupDraw(boolean bindVao) {
		this.validateState();
		this.uniforms.upload();
		if(bindVao) {
			this.vao.bind();
			if(this.ebo == null && this.strategy instanceof AutoElementFamily f && f.byte_ instanceof Seq) {
				return -1; // 1, 2, 3, etc. does not need drawElements
			} else if(this.ebo != null) { // strategy was hotswapped, eg. some section is in quads, some is in triangle
				this.hotswapStrategy(this.strategy, true);
				this.ebo.bind();
				return this.ebo.currentType;
			} else {
				// custom strategy without hotswaps
				ShapeStrat strat = ((AutoElementFamily) this.strategy).forCount(this.vao.last
					.getBuilder()
					.getVertexCount());
				strat.bind();
				return strat.getType();
			}
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		return this.srcId + "#" + this.uniqueId;
	}

	private static int getOrCompileShader(
		SourceProvider src, ShaderPreprocessor libSrc, Object2IntMap<Id> cache, Id sourceId, int type) {
		return cache.computeIfAbsent(sourceId, (Id id) -> createProgram(src, libSrc, type, id));
	}

	private void validateState() {
		if(this.manager.isInvalid) {
			throw new IllegalStateException(this + " was closed!");
		}
	}

	static class InitializationStacktrace extends Throwable {
		public InitializationStacktrace() {
			super("GPU Memory Leak: did not close BareShader instance!");
		}
	}

	public static class ReclamationManager implements Runnable {
		public final StackTraceElement[] init;
		public boolean isInvalid;

		public ReclamationManager(StackTraceElement[] init) {
			this.init = init;
		}

		@Override
		public void run() {
			if(!this.isInvalid) {
				this.isInvalid = true;
				if(this.init == null) {
					System.err.println(
						"GPU Memory Leak: did not close BareShader instance! Use -Djerraria.dev=true to find out " +
						"where you created the leaked instance!");
				} else {
					InitializationStacktrace stacktrace = new InitializationStacktrace();
					stacktrace.setStackTrace(this.init);
					stacktrace.printStackTrace();
				}
			}
		}
	}

	static final class ShaderValidationException extends Exception {
		public ShaderValidationException(String message, Throwable cause) {
			super(message, cause);
		}

		public ShaderValidationException(String message) {
			super(message);
		}
	}

	public static final class GlIdReference {
		public int glId;
	}

	public static final class Uncompiled {
		final Id id;
		final Id frag, vert;
		final Map<String, Field> vertexFields;
		final Map<String, Field> uniformFields;
		final Map<String, Field> outputFields;

		public Uncompiled(Id id, Id frag, Id vert) {
			this.id = id;
			this.frag = frag;
			this.vert = vert;
			this.vertexFields = new HashMap<>();
			this.uniformFields = new HashMap<>();
			this.outputFields = new HashMap<>();
		}

		public void type(
			GlValue.Loc type, DataType local, String name, String groupName, Object extra, boolean isOptional) {
			if(DataType.UNSUPPORTED_TYPES.contains(local)) {
				throw new UnsupportedOperationException(local + " is unsupported on this machine!");
			}

			(switch(type) {
				case UNIFORM -> this.uniformFields;
				case ATTRIBUTE -> this.vertexFields;
				case OUTPUT -> this.outputFields;
			}).put(name, new Field(local, name, groupName, extra, isOptional));
		}
	}

	public record Field(DataType type, String name, String groupName, Object extra, boolean isOptional) {
		public String groupName(boolean isUniform) {
			String name = this.groupName;
			if(name != null || isUniform) {
				return name;
			} else {
				return "default_";
			}
		}

		public boolean isMandatory() {
			return !this.isOptional;
		}
	}
}
