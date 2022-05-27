package net.devtech.jerraria.render.api;

import java.util.ArrayList;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.FrameOut;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.FragOutput;
import net.devtech.jerraria.render.internal.LazyGlData;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.UniformData;
import net.devtech.jerraria.render.internal.VFBuilderImpl;
import net.devtech.jerraria.render.internal.renderhandler.RenderHandler;
import net.devtech.jerraria.util.Id;

/**
 * Implementation of the shader class moved into out to make the main class easier to read
 */
public class ShaderImpl {
	public static <T extends Shader<?>> T createShader(
		Id id, Shader.Copier<T> copyFunction, Shader.Initializer<T> initializer, RenderHandler handler) {
		VFBuilderImpl<End> builder = new VFBuilderImpl<>();
		@SuppressWarnings("unchecked")
		T shader = initializer.create(id, builder, new ShaderInitContext<>(copyFunction, handler));
		compile((Shader<?>) shader);
		if(shader instanceof TranslucentShader && handler == RenderHandler.INSTANCE) {
			throw new UnsupportedOperationException("Use TranslucencyRenderer#create to create TranslucentShaders!");
		}
		return shader;
	}

	public static void emptyFrameBuffer(Shader<?> shader) {
		FragOutput outputs = shader.shader.outputs;
		if(outputs != null) {
			outputs.flushBuffer();
		}
	}

	static <T extends GlValue<?> & GlValue.Attribute> T vert(Shader<T> shader) {
		if(shader.verticesSinceStrategy != 0 && !shader.endedVertex) {
			shader.shader.vao.next();
		}
		shader.endedVertex = false;
		shader.verticesSinceStrategy++;
		return shader.compiled;
	}


	static <T extends GlValue<?> & GlValue.Attribute> Shader<T> strategy(Shader<T> shader, AutoStrat strategy) {
		if(shader.verticesSinceStrategy != 0) {
			validateAndFlushVertex(shader,
				shader.shader.strategy,
				shader.shader.strategy.vertexCount(),
				shader.shader.strategy.minimumVertices()
			);
			shader.verticesSinceStrategy = 0;
		}

		shader.shader.hotswapStrategy(strategy, false);
		return shader;
	}

	static <T extends GlValue<?> & GlValue.Attribute> void drawKeep(Shader<T> shader, BuiltGlState state) {
		if(shader.verticesSinceStrategy == 0) {
			return;
		}
		AutoStrat strategy = shader.getStrategy();
		validateAndFlushVertex(shader, strategy, strategy.vertexCount(), strategy.minimumVertices());
		shader.handler.drawKeep(shader, state);
	}

	static <T extends GlValue<?> & GlValue.Attribute> void drawInstancedKeep(
		Shader<T> shader,
		BuiltGlState state,
		int count) {
		if(shader.verticesSinceStrategy == 0) {
			return;
		}
		AutoStrat strategy = shader.getStrategy();
		validateAndFlushVertex(shader, strategy, strategy.vertexCount(), strategy.minimumVertices());
		shader.handler.drawInstancedKeep(shader, state, count);
	}

	static <U extends AbstractGlValue<?> & GlValue.Uniform> void copyUniform_(U from, U to) {
		GlData fromData = to.data, toData = from.data;
		if(fromData instanceof LazyGlData u) {
			fromData = u.getUniforms();
		}
		if(toData instanceof LazyGlData u) {
			toData = u.getUniforms();
		}
		if(toData instanceof UniformData fromU && fromData instanceof UniformData toU) {
			fromU.copyTo(from.element, toU, to.element);
		} else {
			throw new UnsupportedOperationException("unrecognized copy " + fromData.getClass() + " to " + toData.getClass());
		}
	}

	static <T extends GlValue<?> & GlValue.Attribute, U extends GlValue<End> & GlValue.Uniform> U addUniform(
		Shader<T> shader, GlValue.Type<U> type) {
		if(!shader.isCopy) {
			type.validateUniform();
			if(shader.shader == null) {
				shader.uniforms.add(type);
			} else {
				throw new IllegalStateException("Uniforms must be defined before vertex attributes!");
			}
		}
		return type.create(shader.uniformData, null);
	}

	static FrameOut addOutput(Shader<?> shader, String name, DataType imageType) {
		GlValue.Type<FrameOut> out = FrameOut.out(name, imageType);
		if(!shader.isCopy) {
			out.validateOutput();
			if(shader.shader == null) {
				shader.outputs.add(out);
			} else {
				throw new IllegalStateException("Uniforms must be defined before vertex attributes!");
			}
		}
		return out.create(shader.outData, null);
	}

	static <T extends GlValue<?> & GlValue.Attribute> void compile(Shader<T> shader) {
		BareShader bare = ShaderManager.getShader(shader.id,
			shader.builder.attributes,
			shader.uniforms,
			shader.outputs,
			shader.compilationConfig
		);
		shader.shader = bare;
		Pair<T, End> build = shader.builder.build(bare);
		shader.compiled = build.first();
		shader.end = build.second();
	}

	static <T extends GlValue<?> & GlValue.Attribute> void validateAndFlushVertex(
		Shader<T> shader, Object string, int vertexCount, int minimumVertices) {
		if(shader.verticesSinceStrategy % vertexCount != 0) {
			throw new IllegalArgumentException("Expected multiple of " + vertexCount + " vertexes for " + "rendering " + string + " but found " + shader.verticesSinceStrategy);
		}
		if(shader.verticesSinceStrategy < minimumVertices) {
			throw new IllegalArgumentException("Expected atleast " + minimumVertices + " vertexes for " + "rendering " + string + " but found " + shader.verticesSinceStrategy);
		}
		if(!shader.endedVertex) {
			shader.shader.vao.next();
			shader.endedVertex = true;
		}
	}

	static <T extends GlValue<?> & GlValue.Attribute> void copyPostInit(
		Shader<T> shader, Shader<T> copy, SCopy method) {
		BareShader bare = new BareShader(copy.shader, method);
		shader.copyFunction = copy.copyFunction;
		shader.builder = copy.builder;
		shader.outputs = copy.outputs;
		shader.uniformData = bare.uniforms;
		shader.outData = bare.outputs;
		shader.uniforms = copy.uniforms;
		Pair<T, End> build = copy.builder.build(bare);
		shader.compiled = build.first();
		shader.end = build.second();
		shader.shader = bare;
		shader.isCopy = true;
		shader.compilationConfig.putAll(copy.compilationConfig);
		shader.handler = copy.handler;
	}

	static <T extends GlValue<?> & GlValue.Attribute> void postInit(
		Shader<T> shader, VFBuilderImpl<T> builder, ShaderInitContext copy) {
		shader.builder = builder;
		shader.copyFunction = copy.copier;
		shader.handler = copy.handler;
		shader.uniforms = new ArrayList<>();
		shader.outputs = new ArrayList<>();
		shader.uniformData = new LazyGlData(shader, b -> b.uniforms);
		shader.outData = new LazyGlData(shader, b -> b.outputs);
		shader.isCopy = false;
	}

	record ShaderInitContext<T extends Shader<?>>(Shader.Copier<T> copier, RenderHandler handler) {}
}
