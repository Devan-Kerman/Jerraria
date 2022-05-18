package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.SourceProvider;
import net.devtech.jerraria.render.internal.VFBuilderImpl;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;


/**
 * An object of this class represents a reference to an opengl shader, it's uniform's values, and it's vertex data.
 */
public abstract class Shader<T extends GlValue<?> & GlValue.Attribute> {
	public final Id id;
	final Map<String, Object> compilationConfig = new HashMap<>();
	List<GlValue.Type<?>> uniforms;
	GlData uniformData;
	VFBuilderImpl<T> builder;
	Copier<Shader<?>> copyFunction;
	boolean endedVertex;
	int verticesSinceStrategy;
	T compiled;
	boolean isCopy;
	BareShader shader;
	End end;

	/**
	 * @param builder the builder that has been configured for the generics of this class
	 */
	protected Shader(Id id, VFBuilder<T> builder, Object context) {
		this.id = id;
		ShaderImpl.postInit(this, (VFBuilderImpl<T>) builder, (Copier<Shader<?>>) context);
	}

	/**
	 * Copy constructor
	 */
	protected Shader(Shader<T> shader, SCopy method) {
		this.id = shader.id;
		ShaderImpl.copyPostInit(this, shader, method);
	}

	public static <N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> T createShader(
		Id id,
		Copier<T> copyFunction,
		Initializer<N, T> initializer) {
		return ShaderImpl.createShader(id, copyFunction, initializer);
	}

	/**
	 * Starts writing the next vertex data, you must call all the GlValues in the chain before calling this again!
	 *
	 * @return T the vertex configurator
	 * @see GlValue#getNext()
	 * @see Vec3.F#vec3f(Matrix3f, float, float, float)
	 */
	public final T vert() {
		return ShaderImpl.vert(this);
	}

	/**
	 * Copy the data of the given vertex id into the next vertex
	 *
	 * @param vertexId the id of the vertex to copy {@link End}
	 */
	public final void copy(int vertexId) {
		ShaderImpl.copy(this, vertexId);
	}

	@Contract("_,_->this")
	public final Shader<T> copy(Shader<T> shader, int vertexId) {
		return ShaderImpl.copyVertex(this, shader, vertexId);
	}

	/**
	 * Sets the AutoElementStrategy of this Shader
	 *
	 * @see AutoStrat
	 */
	@Contract("_->this")
	public Shader<T> strategy(AutoStrat strategy) {
		return ShaderImpl.strategy(this, strategy);
	}

	/**
	 * Bind the shader and render its contents
	 */
	public final void render() {
		ShaderImpl.renderNoFlush(this);
	}

	/**
	 * Bind the shader and render its contents X times using opengl's instanced rendering
	 */
	public final void renderInstanced(int count) {
		ShaderImpl.renderInstancedNoFlush(this, count);
	}

	/**
	 * Bind the shader, render its contents, and then delete all it's vertex data
	 */
	public final void renderAndDelete() {
		ShaderImpl.renderAndFlush(this);
	}

	public static <U extends AbstractGlValue<?> & GlValue.Uniform> void copyUniform(U from, U to) {
		ShaderImpl.copyUniform_(from, to);
	}

	public final void deleteVertexData() {
		ShaderImpl.flushVertex(this);
	}

	public final void deleteUniformData() {
		this.shader.uniforms.flush();
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

	public void reload() {
		ShaderManager.reloadShader(this.shader, this.id, this.compilationConfig);
	}

	@ApiStatus.Internal
	public BareShader getShader() {
		return shader;
	}

	/**
	 * @return the uniform configurator for the given variable
	 */
	protected final <U extends GlValue<End> & GlValue.Uniform> U uni(GlValue.Type<U> type) {
		return ShaderImpl.addUniform(this, type);
	}

	/**
	 * Put a custom shader compilation parameter, these are assumed to be processed by a {@link SourceProvider}.
	 * Calling this method after the shader has been built has no effect unless {@link #reload()} is called.
	 *
	 * <p>Existing supported parameters: (none)</p>
	 */
	protected final void putParameter(String name, Object value) {
		this.compilationConfig.put(name, value);
	}

	/**
	 * Add a custom shader compilation parameter, these are assumed to be processed by a {@link SourceProvider}.
	 * Calling this method after the shader has been built has no effect unless {@link #reload()} is called.
	 *
	 * <p>
	 *     Existing supported parameters: <br>
	 *      - "define": adds a "#define [value]"
	 * </p>
	 */
	protected final void addParameter(String name, Object value) {
		Object o = this.compilationConfig.computeIfAbsent(name, a -> new ArrayList<>());
		if(o instanceof Collection c) {
			c.add(value);
		} else {
			throw new IllegalStateException(name + " is not a list!");
		}
	}

	public interface Copier<T extends Shader<?>> {
		T copy(T old, SCopy method);
	}

	public interface Initializer<N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> {
		T create(Id id, VFBuilder<End> builder, Object context);
	}
}
