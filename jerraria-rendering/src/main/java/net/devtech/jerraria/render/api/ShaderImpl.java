package net.devtech.jerraria.render.api;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.element.AutoElementFamily;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.TypesInternalAccess;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.UniformData;
import net.devtech.jerraria.render.internal.VFBuilderImpl;
import net.devtech.jerraria.render.internal.element.Seq;
import net.devtech.jerraria.util.Id;

/**
 * Implementation of the shader class moved into out to make the main class easier to read
 */
class ShaderImpl {
	static <N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> T createShader(
		Id id, Shader.Copier<T> copyFunction, Shader.Initializer<N, T> initializer) {
		VFBuilderImpl<End> builder = new VFBuilderImpl<>();
		@SuppressWarnings("unchecked")
		T shader = initializer.create(id, builder, copyFunction);
		compile(shader);
		return shader;
	}

	static <T extends GlValue<?> & GlValue.Attribute> T vert(Shader<T> shader) {
		if(shader.verticesSinceStrategy != 0 && !shader.endedVertex) {
			shader.shader.vao.next();
		}
		shader.endedVertex = false;
		TypesInternalAccess.setVertexId(shader.end, shader.verticesSinceStrategy++);
		return shader.compiled;
	}

	static void copy(Shader<?> shader, int vertexId) {
		if(shader.verticesSinceStrategy != 0 && !shader.endedVertex) {
			shader.shader.vao.next();
		}

		if(shader.shader.ebo != null && shader.getStrategy() instanceof AutoElementFamily f && f.byte_ instanceof Seq) {
			shader.shader.ebo.append(vertexId);
		} else {
			shader.shader.vao.copy(vertexId);
			shader.endedVertex = false;
		}
		shader.verticesSinceStrategy++;
	}

	static <T extends GlValue<?> & GlValue.Attribute> Shader<T> getShader(
		Shader<T> current,
		Shader<T> shader,
		int vertexId) {
		if(current.verticesSinceStrategy != 0 && !current.endedVertex) {
			current.shader.vao.next();
		}
		current.endedVertex = false;
		current.shader.vao.copy(shader.shader.vao, vertexId);
		return current;
	}

	static <T extends GlValue<?> & GlValue.Attribute> Shader<T> strategy(Shader<T> shader, AutoStrat strategy) {
		if(shader.verticesSinceStrategy != 0) {
			validateAndFlushVertex(shader, shader.shader.strategy);
			shader.verticesSinceStrategy = 0;
		}

		shader.shader.hotswapStrategy(strategy, false);
		return shader;
	}

	static <T extends GlValue<?> & GlValue.Attribute> void renderNoFlush(Shader<T> shader) {
		if(shader.verticesSinceStrategy == 0)
			return;
		validateAndFlushVertex(shader, shader.getStrategy());
		shader.shader.draw();
	}

	static <T extends GlValue<?> & GlValue.Attribute> void renderInstancedNoFlush(Shader<T> shader, int count) {
		if(shader.verticesSinceStrategy == 0)
			return;
		validateAndFlushVertex(shader, shader.getStrategy());
		shader.shader.drawInstanced(count);
	}

	static <T extends GlValue<?> & GlValue.Attribute> void renderAndFlush(Shader<T> shader) {
		if(shader.verticesSinceStrategy == 0)
			return;
		shader.render();
		shader.deleteVertexData();
	}

	static <U extends AbstractGlValue<?> & GlValue.Uniform> void copyUniform_(U from, U to) {
		if(to.data instanceof UniformData fromU && from.data instanceof UniformData toU) {
			fromU.copyTo(from.element, toU, to.element);
		}
	}

	static <T extends GlValue<?> & GlValue.Attribute> void flushVertex(Shader<T> shader) {
		shader.shader.deleteVertexData();
		shader.verticesSinceStrategy = 0;
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

	static <T extends GlValue<?> & GlValue.Attribute> void compile(Shader<T> shader) {
		BareShader bare = ShaderManager.getBareShader(shader.id, shader.builder.attributes, shader.uniforms);
		shader.shader = bare;
		Pair<T, End> build = shader.builder.build(bare);
		shader.compiled = build.first();
		shader.end = build.second();
	}

	static <T extends GlValue<?> & GlValue.Attribute> void validateAndFlushVertex(Shader<T> shader, AutoStrat strategy) {
		validateAndFlushVertex(shader, strategy, strategy.vertexCount(), strategy.minimumVertices());
	}

	static <T extends GlValue<?> & GlValue.Attribute> void validateAndFlushVertex(
		Shader<T> shader,
		Object string,
		int vertexCount,
		int minimumVertices) {
		if(shader.verticesSinceStrategy % vertexCount != 0) {
			throw new IllegalArgumentException("Expected multiple of " + vertexCount + " vertexes for " +
			                                   "rendering " + string + " but found " + shader.verticesSinceStrategy);
		}
		if(shader.verticesSinceStrategy < minimumVertices) {
			throw new IllegalArgumentException("Expected atleast " + minimumVertices + " vertexes for " +
			                                   "rendering " + string + " but found " + shader.verticesSinceStrategy);
		}
		endOfVertex(shader);
	}

	static <T extends GlValue<?> & GlValue.Attribute> void endOfVertex(Shader<T> shader) {
		if(!shader.endedVertex) {
			shader.shader.vao.next();
			shader.endedVertex = true;
		}
	}

	static <T extends GlValue<?> & GlValue.Attribute> void postCopyInit(
		Shader<T> init, Shader<T> shader, BareShader bare) {
		Pair<T, End> build = shader.builder.build(bare);
		init.compiled = build.first();
		init.end = build.second();
		init.shader = bare;
		init.isCopy = true;
	}
}
