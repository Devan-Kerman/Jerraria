package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.GlData;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.render.internal.VFBuilderImpl;

public abstract class Shader<T extends GlValue<?> & GlValue.Attribute> {
	public final Id id;

	final List<GlValue.Type<?>> uniforms;
	final GlData uniformData;
	final VFBuilderImpl<T> builder;
	final ShaderCopier<Shader<T>> copyFunction;

	boolean finalized;
	int vertices;
	BareShader shader;
	T compiled;
	boolean isCopy;

	public interface ShaderCopier<T extends Shader<?>> {
		T copy(T old, SCopy method);
	}

	public interface ShaderInitializer<N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> {
		T create(Id id, VFBuilder<End> builder, Object context);
	}

	public static <N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> T createShader(Id id, ShaderCopier<T> copyFunction, ShaderInitializer<N, T> initializer) {
		VFBuilderImpl<End> builder = new VFBuilderImpl<>();
		@SuppressWarnings("unchecked")
		T shader = initializer.create(id, builder, copyFunction);
		shader.compile();
		return shader;
	}

	public static <N extends GlValue<?> & GlValue.Attribute, T extends Shader<N>> T copy(T shader, SCopy method) {
		//noinspection unchecked
		return (T) shader.copyFunction.copy(shader, method);
	}

	public Shader(Id id, VFBuilder<T> builder, Object context) {
		this.id = id;
		this.builder = (VFBuilderImpl<T>) builder;
		this.copyFunction = (ShaderCopier<Shader<T>>) context;
		this.uniformData = new LazyUniformData();
		this.uniforms = new ArrayList<>();
		this.isCopy = false;
	}

	void compile() {
		BareShader shader = ShaderManager.getBareShader(this.id, this.builder.attributes, this.uniforms);
		this.shader = shader;
		this.compiled = this.builder.build(shader);
	}

	public Shader(Shader<T> shader, SCopy method) {
		this.copyFunction = shader.copyFunction;
		this.id = shader.id;
		BareShader bare = new BareShader(shader.shader, method);
		this.shader = bare;
		this.uniformData = bare.uniforms;
		this.uniforms = shader.uniforms;
		this.compiled = shader.builder.build(bare);
		this.builder = shader.builder;
		this.isCopy = true;
	}

	public final void render(Primitive primitive) {
		if(this.vertices % primitive.vertexCount != 0) {
			throw new IllegalArgumentException("Expected multiple of " + primitive.vertexCount + " vertexes for rendering " + primitive + " but found " + this.vertices);
		}
		this.endOfVertex();
		this.shader.draw(primitive.glId);
	}

	public final void renderAndFlush(Primitive primitive) {
		this.render(primitive);
		this.shader.vao.flush();
	}

	private void endOfVertex() {
		if(!this.finalized) {
			this.shader.vao.next();
			this.finalized = true;
		}
	}

	public final T vert() {
		if(this.vertices != 0 && !this.finalized) {
			this.shader.vao.next();
		}
		this.finalized = false;
		this.vertices++;
		return this.compiled;
	}

	protected final <U extends GlValue<End>> U uni(GlValue.Type<U> type) {
		if(!this.isCopy) {
			if(this.shader == null) {
				this.uniforms.add(type);
			} else {
				throw new IllegalStateException("Uniforms must be defined before vertex attributes!");
			}
		}
		return type.create(this.uniformData, null);
	}

	final class LazyUniformData extends GlData {
		@Override
		public GlData flush() {
			return Shader.this.shader.uniforms.flush();
		}

		@Override
		public Buf element(Element element) {
			if(element instanceof LazyElement l) {
				element = l.getValue();
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

	static final class LazyElement implements GlData.Element {
		Shader<?> shader;
		String name;
		GlData.Element value;

		public LazyElement(Shader<?> shader, String name) {
			this.shader = shader;
			this.name = name;
		}

		public GlData.Element getValue() {
			GlData.Element value = this.value;
			if(value == null) {
				this.value = value = this.shader.shader.uniforms.getElement(this.name);
				this.name = null;
				this.shader = null;
			}
			return value;
		}
	}
}
