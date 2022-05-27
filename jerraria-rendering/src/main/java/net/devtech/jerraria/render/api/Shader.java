package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.FrameOut;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.SourceProvider;
import net.devtech.jerraria.render.internal.VFBuilderImpl;
import net.devtech.jerraria.render.internal.renderhandler.RenderHandler;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;


/**
 * An object of this class represents a reference to an opengl shader, it's uniform's values, and it's vertex data.
 */
public abstract class Shader<T extends GlValue<?> & GlValue.Attribute> {
	// todo ticking for Instancer
	// todo custom invalidation for Instancer
	// todo more flexible "allocation" of instances
	// todo effecient copy commands (lazily evaluated, and can operate on whole ranges)

	public final Id id;
	final Map<String, Object> compilationConfig = new HashMap<>();
	RenderHandler handler;
	List<GlValue.Type<?>> uniforms, outputs;
	GlData uniformData, outData;
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
		ShaderImpl.postInit(this, (VFBuilderImpl<T>) builder, (ShaderImpl.ShaderInitContext) context);
	}

	/**
	 * Copy constructor
	 */
	protected Shader(Shader<T> shader, SCopy method) {
		this.id = shader.id;
		ShaderImpl.copyPostInit(this, shader, method);
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
		return ShaderImpl.vert(this);
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
	 * Bind the shader and render and retain its contents to be redrawn another time.
	 *
	 * @param state the gl state to use prior to drawing
	 * @see #draw(BuiltGlState)
	 */
	public final void drawKeep(BuiltGlState state) {
		this.preRender(RenderCall.DRAW);
		ShaderImpl.drawKeep(this, state);
		this.postRender(RenderCall.DRAW);
	}

	public final void drawKeep() {
		this.drawKeep(this.handler.defaultGlState());
	}

	/**
	 * Bind the shader and render its contents X times using opengl's instanced rendering and retain its contents to be redrawn another time.
	 */
	public final void drawInstancedKeep(BuiltGlState state, int count) {
		this.preRender(RenderCall.DRAW_INSTANCED);
		ShaderImpl.drawInstancedKeep(this, state, count);
		this.postRender(RenderCall.DRAW_INSTANCED);
	}

	public final void drawInstancedKeep(int instances) {
		this.drawInstancedKeep(this.handler.defaultGlState(), instances);
	}

	/**
	 * Bind the shader, render its contents, and then delete all it's vertex data
	 */
	public final void draw(BuiltGlState state) {
		this.drawKeep(state);
		this.deleteVertexData();
	}

	public final void draw() {
		this.draw(this.handler.defaultGlState());
	}

	public BuiltGlState defaultState() {
		return this.handler.defaultGlState();
	}

	public static <U extends AbstractGlValue<?> & GlValue.Uniform> void copyUniform(U from, U to) {
		ShaderImpl.copyUniform_(from, to);
	}

	public final void deleteVertexData() {
		this.shader.deleteVertexData();
		this.verticesSinceStrategy = 0;
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
		return this.shader;
	}

	public void flushFrameBuffer() { // todo SETTINGS
		ShaderImpl.emptyFrameBuffer(this);
	}

	protected void preRender(RenderCall call) {
	}

	protected void postRender(RenderCall call) {
	}

	/**
	 * @return the uniform configurator for the given variable
	 */
	protected final <U extends GlValue<End> & GlValue.Uniform> U uni(GlValue.Type<U> type) {
		return ShaderImpl.addUniform(this, type);
	}

	protected final FrameOut addOutput(String name, DataType imageType) {
		return ShaderImpl.addOutput(this, name, imageType);
	}

	protected final FrameOut imageOutput(String name) {
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
		this.compilationConfig.put(name, value);
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
		Object o = this.compilationConfig.computeIfAbsent(name, a -> new ArrayList<>());
		if(o instanceof Collection c) {
			c.add(value);
		} else {
			throw new IllegalStateException(name + " is not a list!");
		}
	}

	protected enum RenderCall {
		DRAW(false), DRAW_INSTANCED(true);

		public final boolean isInstanced;

		RenderCall(boolean instanced) {
			this.isInstanced = instanced;
		}
	}

	public interface Copier<T extends Shader<?>> {
		T copy(T old, SCopy method);
	}

	public interface Initializer<T extends Shader<?>> {
		T create(Id id, VFBuilder<End> builder, Object context);
	}
}
