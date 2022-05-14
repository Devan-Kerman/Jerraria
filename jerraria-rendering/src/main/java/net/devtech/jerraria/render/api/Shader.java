package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.TypesInternalAccess;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.LazyElement;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.UniformData;
import net.devtech.jerraria.render.internal.VFBuilderImpl;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;

/**
 * An object of this class represents a reference to an opengl shader, it's uniform's values, and it's vertex data.
 */
public abstract class Shader<T extends GlValue<?> & GlValue.Attribute> {
	// todo EBOs
		// copy vertex based on ID (both with EBO and without)
		// AutoElementStrategy for automatic quads, support hotswapping AutoElementStrategies
		// add vertex data in EBOs without creating element (so u can add all the vertex data and then manually copy)

	public final Id id;
	final List<GlValue.Type<?>> uniforms;
	final GlData uniformData;
	final VFBuilderImpl<T> builder;
	final ShaderCopier<Shader<?>> copyFunction;
	boolean endedVertex;
	int vertices;
	T compiled;
	boolean isCopy;
	private BareShader shader;
	private End end;

	/**
	 * @param builder the builder that has been configured for the generics of this class
	 */
	protected Shader(Id id, VFBuilder<T> builder, Object context) {
		this.id = id;
		this.builder = (VFBuilderImpl<T>) builder;
		this.copyFunction = (ShaderCopier<Shader<?>>) context;
		this.uniformData = new LazyUniformData();
		this.uniforms = new ArrayList<>();
		this.isCopy = false;
	}

	/**
	 * Copy constructor
	 */
	protected Shader(Shader<T> shader, SCopy method) {
		this.copyFunction = shader.copyFunction;
		this.id = shader.id;
		BareShader bare = new BareShader(shader.shader, method);
		this.shader = bare;
		this.uniformData = bare.uniforms;
		this.uniforms = shader.uniforms;
		Pair<T, End> build = shader.builder.build(bare);
		this.compiled = build.first();
		this.end = build.second();
		this.builder = shader.builder;
		this.isCopy = true;
	}

	public static <N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> T createShader(
		Id id, ShaderCopier<T> copyFunction, ShaderInitializer<N, T> initializer) {
		VFBuilderImpl<End> builder = new VFBuilderImpl<>();
		@SuppressWarnings("unchecked")
		T shader = initializer.create(id, builder, copyFunction);
		shader.compile();
		return shader;
	}

	/**
	 * Starts a new vertex, you must call all the GlValues in the chain before calling this again!
	 * @see GlValue#getNext()
	 * @see Vec3.F#vec3f(Matrix3f, float, float, float)
	 * @return T the vertex configurator
	 */
	public final T vert() {
		if(this.vertices != 0 && !this.endedVertex) {
			this.shader.vao.next();
		}
		this.endedVertex = false;
		TypesInternalAccess.setVertexId(this.end, this.vertices++);
		return this.compiled;
	}

	public Shader<T> strategy(AutoStrat strategy) {
		this.shader.hotswapStrategy(strategy, false);
		return this;
	}

	/**
	 * Bind the shader and render its contents
	 */
	public final void render() {
		if(this.vertices == 0) return;
		this.preRender(this.shader.strategy.getDrawMethod());
		this.shader.draw();
	}

	/**
	 * Bind the shader and render its contents X times using opengl's instanced rendering
	 */
	public final void renderInstanced(int count) {
		if(this.vertices == 0) return;
		this.preRender(this.shader.strategy.getDrawMethod());
		this.shader.drawInstanced(count);
	}

	/**
	 * Bind the shader, render its contents, and then delete all it's vertex data
	 */
	public final void renderAndDelete() {
		if(this.vertices == 0) return;
		this.render();
		this.deleteVertexData();
	}

	public static <U extends AbstractGlValue<?> & GlValue.Uniform> void copyUniform(U from, U to) {
		if(to.data instanceof UniformData fromU && from.data instanceof UniformData toU) {
			fromU.copyTo(from.element, toU, to.element);
		}
	}

	public final void deleteVertexData() {
		this.shader.deleteVertexData();
	}

	public final void deleteUniformData() {
		this.shader.deleteVertexData();
	}

	/**
	 * Creates a new shader object that shares the opengl shader but has its own uniforms and vertex data. This is
	 * useful for when you want to save vertex data without worrying about the main instance being modified, e.g. chunk
	 * baking.
	 */
	public static <T extends Shader<?>> T copy(T shader, SCopy method) {
		//noinspection unchecked
		return (T) shader.copyFunction.copy(shader, method);
	}

	public AutoStrat getStrategy() {
		return this.shader.strategy;
	}

	/**
	 * @return the uniform configurator for the given variable
	 */
	protected final <U extends GlValue<End> & GlValue.Uniform> U uni(GlValue.Type<U> type) {
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

	void compile() {
		BareShader shader = ShaderManager.getBareShader(this.id, this.builder.attributes, this.uniforms);
		this.shader = shader;
		Pair<T, End> build = this.builder.build(shader);
		this.compiled = build.first();
		this.end = build.second();
	}

	public void reload() {
		ShaderManager.reloadShader(this.shader, this.id);
	}

	void preRender(DrawMethod primitive) {
		if(this.vertices % primitive.vertexCount != 0) {
			throw new IllegalArgumentException("Expected multiple of " + primitive.vertexCount + " vertexes for " +
			                                   "rendering " + primitive + " but found " + this.vertices);
		}
		if(this.vertices < primitive.minimumVertices) {
			throw new IllegalArgumentException("Expected atleast " + primitive.minimumVertices + " vertexes for " +
			                                   "rendering " + primitive + " but found " + this.vertices);
		}
		this.endOfVertex();
	}

	void endOfVertex() {
		if(!this.endedVertex) {
			this.shader.vao.next();
			this.endedVertex = true;
		}
	}

	public interface ShaderCopier<T extends Shader<?>> {
		T copy(T old, SCopy method);
	}

	public interface ShaderInitializer<N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> {
		T create(Id id, VFBuilder<End> builder, Object context);
	}

	@ApiStatus.Internal
	public BareShader getShader() {
		return shader;
	}

	final class LazyUniformData extends GlData {

		@Override
		public Buf element(Element element) {
			if(element instanceof LazyElement l) {
				element = l.getSelf();
			}
			return Shader.this.shader.uniforms.element(element);
		}

		@Override
		public Element getElement(String name) {
			BareShader shader = Shader.this.shader;
			if(shader != null) {
				return shader.uniforms.getElement(name);
			} else {
				return new LazyElement(Shader.this, name);
			}
		}
	}
}
