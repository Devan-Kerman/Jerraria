package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.impl.RenderingEnvironment;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Out;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.FragmentOutputData;
import net.devtech.jerraria.render.internal.LazyGlData;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.StructTypeImpl;
import net.devtech.jerraria.render.internal.VFBuilderImpl;
import net.devtech.jerraria.render.internal.arr.ShaderBufferImpl;
import net.devtech.jerraria.render.internal.renderhandler.RenderHandler;
import net.devtech.jerraria.util.Id;

/**
 * Implementation of the shader class moved into out to make the main class easier to read
 */
public class ShaderImpl<T extends GlValue<?> & GlValue.Attribute> {
	final Id id;
	final Map<String, Object> compilationConfig = new HashMap<>();
	final RenderHandler handler;
	final List<GlValue.Type<?>> uniforms, outputs;
	final GlData uniformData, outData;
	final VFBuilderImpl<T> builder;
	final Shader.Copier<Shader<?>> copyFunction;
	final boolean isCopy;
	int verticesSinceStrategy;
	T compiled;
	BareShader shader;
	End end;

	public ShaderImpl(ShaderImpl<T> copy, SCopy method) {
		BareShader bare = new BareShader(copy.shader, method);
		this.verticesSinceStrategy = copy.verticesSinceStrategy;
		this.copyFunction = copy.copyFunction;
		this.builder = copy.builder;
		this.outputs = copy.outputs;
		this.uniformData = bare.uniforms;
		this.outData = bare.outputs;
		this.uniforms = copy.uniforms;
		Pair<T, End> build = copy.builder.build(bare);
		this.compiled = build.first();
		this.end = build.second();
		this.shader = bare;
		this.isCopy = true;
		this.compilationConfig.putAll(copy.compilationConfig);
		this.handler = copy.handler;
		this.id = copy.id;
		BareShader.GL_CLEANUP.register(this, bare.manager);
	}

	public ShaderImpl(VFBuilder<T> builder, ShaderInitContext ctx) {
		this.builder = (VFBuilderImpl<T>) builder;
		this.copyFunction = ctx.copier;
		this.handler = ctx.handler;
		this.uniforms = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.uniformData = new LazyGlData(this, b -> b.uniforms);
		this.outData = new LazyGlData(this, b -> b.outputs);
		this.isCopy = false;
		this.id = ctx.id;
	}

	public static <T extends Shader<?>> T createShader(
		Id id, Shader.Copier<T> copyFunction, Shader.Initializer<T> initializer, RenderHandler handler) {
		VFBuilderImpl<End> builder = new VFBuilderImpl<>();
		T shader = initializer.create(builder, new ShaderInitContext<>(id, copyFunction, handler));
		shader.delegate.compile();
		if(shader instanceof TranslucentShader && handler == RenderHandler.INSTANCE) {
			throw new UnsupportedOperationException("Use TranslucencyRenderer#create to create TranslucentShaders!");
		}
		return shader;
	}

	public BareShader getShader() {
		return this.shader;
	}

	public void bake() {
		RenderingEnvironment.validateRenderThread("bake");
		this.getShader().vao.bake();
	}

	public boolean isValid() {
		return !this.shader.manager.isInvalid;
	}

	static void copyUniform_(GlValue<?> from, GlValue<?> to) {
		((GlValue.Copiable)from).copyTo(to);
	}

	void emptyFrameBuffer() {
		FragmentOutputData outputs = this.shader.outputs;
		if(outputs != null) {
			outputs.flushBuffer();
		}
	}

	void reload() {
		ShaderManager.reloadShader(this.shader, this.id, this.compilationConfig);
	}

	AutoStrat getStrategy() {
		return this.getShader().strategy;
	}

	BuiltGlState defaultGlState() {
		return this.handler.defaultGlState();
	}

	T vert() {
		this.shader.vao.vert();
		this.verticesSinceStrategy++;
		return this.compiled;
	}

	void strategy(AutoStrat strategy) {
		if(this.verticesSinceStrategy != 0) {
			this.validate(this.shader.strategy,
				this.shader.strategy.vertexCount(),
				this.shader.strategy.minimumVertices()
			);
			this.verticesSinceStrategy = 0;
		}

		this.shader.hotswapStrategy(strategy, false);
	}

	void deleteVertexData() {
		this.shader.deleteVertexData();
		this.verticesSinceStrategy = 0;
	}

	void drawKeep(Shader<?> shader, BuiltGlState state) {
		RenderingEnvironment.validateRenderThread("draw");
		AutoStrat strategy = shader.getStrategy();
		this.validate(strategy, strategy.vertexCount(), strategy.minimumVertices());
		this.handler.drawKeep(shader, state);
	}

	void drawInstancedKeep(Shader<T> shader, BuiltGlState state, int count) {
		RenderingEnvironment.validateRenderThread("drawInstanced");
		AutoStrat strategy = shader.getStrategy();
		this.validate(strategy, strategy.vertexCount(), strategy.minimumVertices());
		this.handler.drawInstancedKeep(shader, state, count);
	}

	<U extends GlValue<End> & GlValue.Uniform> U addUniform(GlValue.Type<U> type) {
		if(!this.isCopy) {
			type.validateUniform();
			if(this.shader == null) {
				this.uniforms.add(type);
			} else {
				throw new IllegalStateException("Uniforms must be defined before vertex attributes!");
			}
		}
		return type.create(this.uniformData, null);
	}

	<U extends GlValue<?> & GlValue.Uniform> ShaderBuffer<U> buffer(String name, Shader.BufferFunction<U> type) {
		ShaderBufferImpl<U> array = new ShaderBufferImpl<>(this.uniformData, name, type, Integer.MAX_VALUE);
		if(!this.isCopy) {
			this.uniforms.add(array.new ArrayGlValue());
		}
		return array;
	}

	<U extends GlValue<End> & GlValue.Uniform> List<U> array(String name, Function<String, GlValue.Type<U>> initializer, int len) {
		List<U> list = new ArrayList<>();
		for(int i = 0; i < len; i++) {
			list.add(this.addUniform(initializer.apply(name + "[" + i + "]")));
		}
		return Collections.unmodifiableList(list);
	}

	Out addOutput(String name, DataType imageType) {
		GlValue.Type<Out> out = Out.out(name, imageType);
		if(!this.isCopy) {
			out.validateOutput();
			if(this.shader == null) {
				this.outputs.add(out);
			} else {
				throw new IllegalStateException("Uniforms must be defined before vertex attributes!");
			}
		}
		return out.create(this.outData, null);
	}

	void compile() {
		BareShader bare = ShaderManager.getShader(this.id,
			this.builder.attributes,
			this.uniforms,
			this.outputs,
			this.compilationConfig
		);
		this.shader = bare;
		Pair<T, End> build = this.builder.build(bare);
		this.compiled = build.first();
		this.end = build.second();
	}

	void validate(
		Object string, int vertexCount, int minimumVertices) {
		if(this.verticesSinceStrategy % vertexCount != 0) {
			throw new IllegalArgumentException("Expected multiple of " + vertexCount + " vertexes for " + "rendering " + string + " but found " + this.verticesSinceStrategy);
		}
		if(this.verticesSinceStrategy < minimumVertices) {
			throw new IllegalArgumentException("Expected atleast " + minimumVertices + " vertexes for " + "rendering " + string + " but found " + this.verticesSinceStrategy);
		}
	}

	record ShaderInitContext<T extends Shader<?>>(Id id, Shader.Copier<T> copier, RenderHandler handler) {}

	public static List<GlValue.Type<?>> fields(Struct struct) {
		return struct.fields;
	}

	public static void set(Struct struct, StructTypeImpl<?> names) {
		struct.type = names;
	}
}
