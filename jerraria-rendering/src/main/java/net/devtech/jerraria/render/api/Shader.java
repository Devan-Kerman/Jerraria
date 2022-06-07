package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.unimi.dsi.fastutil.Function;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Out;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.SourceProvider;
import net.devtech.jerraria.render.internal.arr.ListShaderBufferImpl;
import net.devtech.jerraria.render.internal.renderhandler.RenderHandler;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;


/**
 * An object of this class represents a reference to an opengl shader, it's uniform's values, and it's vertex data.
 */
public abstract class Shader<T extends GlValue<?> & GlValue.Attribute> implements AutoCloseable {
	// todo effecient copy commands (lazily evaluated, and can operate on whole ranges)
	final ShaderImpl<T> delegate;

	/**
	 * @param builder the builder that has been configured for the generics of this class
	 */
	protected Shader(VFBuilder<T> builder, Object context) {
		this.delegate = new ShaderImpl<>(builder, (ShaderImpl.ShaderInitContext) context);
	}

	/**
	 * Copy constructor
	 */
	protected Shader(Shader<T> shader, SCopy method) {
		this.delegate = new ShaderImpl<>(shader.delegate, method);
	}

	public static <T extends Shader<?>> T create(Id id, Copier<T> copyFunction, Initializer<T> initializer) {
		return ShaderImpl.createShader(id, copyFunction, initializer, RenderHandler.INSTANCE);
	}

	/**
	 * Starts writing the next vertex data, you must exec all the GlValues in the chain before calling this again!
	 *
	 * @return T the vertex configurator
	 * @see GlValue#getNext()
	 * @see Vec3.F#vec3f(Matrix3f, float, float, float)
	 */
	public final T vert() {
		return this.delegate.vert();
	}

	/**
	 * Sets the AutoElementStrategy of this Shader
	 *
	 * @see AutoStrat
	 */
	@Contract("_->this")
	public Shader<T> strategy(AutoStrat strategy) {
		this.delegate.strategy(strategy);
		return this;
	}

	/**
	 * Bind the shader and render and retain its contents to be redrawn another time.
	 *
	 * @param state the gl state to use prior to drawing
	 * @see #draw(BuiltGlState)
	 */
	public final void drawKeep(BuiltGlState state) {
		this.preRender(RenderCall.DRAW);
		this.delegate.drawKeep(this, state);
		this.postRender(RenderCall.DRAW);
	}

	public final void drawKeep() {
		this.delegate.drawKeep(this, this.delegate.defaultGlState());
	}

	/**
	 * Bind the shader and render its contents X times using opengl's instanced rendering and retain its contents to be
	 * redrawn another time.
	 */
	public final void drawInstancedKeep(BuiltGlState state, int count) {
		this.preRender(RenderCall.DRAW_INSTANCED);
		this.delegate.drawInstancedKeep(this, state, count);
		this.postRender(RenderCall.DRAW_INSTANCED);
	}

	public final void drawInstancedKeep(int instances) {
		this.drawInstancedKeep(this.delegate.defaultGlState(), instances);
	}

	/**
	 * Bind the shader, render its contents, and then delete all it's vertex data
	 */
	public final void draw(BuiltGlState state) {
		this.drawKeep(state);
		this.deleteVertexData();
	}

	public final void drawInstanced(BuiltGlState state, int instances) {
		this.drawInstancedKeep(state, instances);
		this.deleteVertexData();
	}

	public final void drawInstanced(int instances) {
		this.drawInstanced(this.delegate.defaultGlState(), instances);
	}

	public final void draw() {
		this.draw(this.delegate.defaultGlState());
	}

	public BuiltGlState defaultState() {
		return this.delegate.defaultGlState();
	}

	public static <U extends GlValue<?> & GlValue.Uniform & GlValue.Copiable> void copyUniform(U from, U to) { // todo allow copying structs
		ShaderImpl.copyUniform_(from, to);
	}

	public final void deleteVertexData() {
		this.delegate.deleteVertexData();
	}

	/**
	 * Creates a new shader object that shares the opengl shader but has its own uniforms and vertex data. This is
	 * useful for when you want to save vertex data without worrying about the main instance being modified, e.g. chunk
	 * baking.
	 */
	public static <T extends Shader<?>> T copy(T shader, SCopy method) {
		//noinspection unchecked
		return (T) shader.delegate.copyFunction.copy(shader, method);
	}

	public AutoStrat getStrategy() {
		return this.delegate.getStrategy();
	}

	public void reload() {
		this.delegate.reload();
	}

	@ApiStatus.Internal
	public BareShader getShader() {
		return this.delegate.shader;
	}

	public void flushFrameBuffer() {
		this.delegate.emptyFrameBuffer();
	}

	/**
	 * Tells the current shader instance that the vertex data of this shader will <b>likely</b> remain unchanged.
	 * Changing the data after baking will incur a significant performance penalty, and should only be done on the render thread.
	 */
	public final void bake() {
		this.delegate.bake();
	}

	@Override
	public void close() {
		try {
			this.delegate.shader.close();
		} catch(Exception e) {
			throw Validate.rethrow(e);
		}
	}

	public Id getId() {
		return this.delegate.id;
	}

	protected void preRender(RenderCall call) {
	}

	protected void postRender(RenderCall call) {
	}

	/**
	 * @return the uniform configurator for the given variable
	 */
	protected final <U extends GlValue<End> & GlValue.Uniform> U uni(GlValue.Type<U> type) {
		return this.delegate.addUniform(type);
	}

	/**
	 * @return the uniform configurator for the given variable
	 */
	protected final <U extends GlValue<End> & GlValue.Uniform> ShaderBuffer<U> buffer(String name, BufferFunction<U> type) {
		return this.delegate.buffer(name, type);
	}

	protected final <U extends GlValue<End> & GlValue.Uniform> ShaderBuffer<U> list(String name, Function<String, GlValue.Type<U>> initializer,
	                                                                                int len) {
		return new ListShaderBufferImpl<>(this.array(name, initializer, len));
	}

	protected final <U extends GlValue<End> & GlValue.Uniform> List<U> array(
		String name,
		Function<String, GlValue.Type<U>> initializer,
		int len) {
		return this.delegate.array(name, initializer, len);
	}

	protected final Out addOutput(String name, DataType imageType) {
		return this.delegate.addOutput(name, imageType);
	}

	protected final Out imageOutput(String name) {
		return this.addOutput(name, DataType.TEXTURE_2D);
	}

	/**
	 * Put a custom shader compilation parameter, these are assumed to be processed by a {@link SourceProvider}.
	 * Calling
	 * this method after the shader has been built has no effect unless {@link #reload()} is called.
	 *
	 * <p>Existing supported parameters: (none)</p>
	 */
	protected final void putParameter(String name, Object value) {
		this.delegate.compilationConfig.put(name, value);
	}

	/**
	 * Add a custom shader compilation parameter, these are assumed to be processed by a {@link SourceProvider}.
	 * Calling
	 * this method after the shader has been built has no effect unless {@link #reload()} is called.
	 *
	 * <p>
	 * Existing supported parameters: <br> - "define": adds a "#define [value]"
	 * </p>
	 */
	protected final void addParameter(String name, Object value) {
		Object o = this.delegate.compilationConfig.computeIfAbsent(name, a -> new ArrayList<>());
		if(o instanceof Collection c) {
			c.add(value);
		} else {
			throw new IllegalStateException(name + " is not a offsets!");
		}
	}

	protected enum RenderCall {
		DRAW(false), DRAW_INSTANCED(true);

		public final boolean isInstanced;

		RenderCall(boolean instanced) {
			this.isInstanced = instanced;
		}
	}

	public interface BufferFunction<U extends GlValue<?>> {
		GlValue.Type<U> apply(String name);
	}

	public interface Copier<T extends Shader<?>> {
		T copy(T old, SCopy method);
	}

	public interface Initializer<T extends Shader<?>> {
		T create(VFBuilder<End> builder, Object context);
	}
}
