package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.internal.StructTypeImpl;
import net.devtech.jerraria.render.internal.UniformData;

/**
 * A struct in the glsl codebase, this is useful as it's {@link Shader#copyUniform(GlValue, GlValue)}} function is
 * optimized to current every field in one go instead of having to {@link Shader#copyUniform(GlValue, GlValue)} for each
 * field.
 */
public abstract class Struct extends GlValue<End> implements GlValue.Uniform, GlValue.Copiable, GlValue.Indexable {
	protected final String name;
	final List<GlValue.Type<?>> fields;
	final List<GlValue<?>> uniforms = new ArrayList<>();
	int index = -3;
	StructTypeImpl<?> type;

	protected Struct(GlData data, GlValue next, String structName) {
		super(data, next);
		this.fields = data instanceof StructTypeImpl.DummyGlData ? new ArrayList<>() : null;
		this.name = structName;
	}

	/**
	 * The {@link Struct}'s analog for {@link GlValue#simple(SimpleType, DataType, String, String)}
	 *
	 * @implSpec Stores common data for this struct, this basically stores whether the struct can be efficiently copied
	 * 	and how to do so.
	 * @see TypeFactory#named(String)
	 */
	public static <T extends Struct> TypeFactory<T> factory(Initializer<T> initializer) {
		return new StructTypeImpl.FactoryImpl<>(initializer);
	}

	@Override
	public void copyTo(GlValue value) {
		this.copyTo((Struct) value);
	}

	@Override
	public int getIndex() {
		int index = this.index;
		if(index == -3) {
			int i = -3;
			for(GlValue<?> uniform : this.uniforms) {
				if(uniform instanceof Indexable idxa) {
					int j = idxa.getIndex();
					if(j != i && i != -3) {
						return this.index = -1;
					} else {
						i = j;
					}
				}
			}
			return this.index = i;
		}
		return index;
	}

	/**
	 * If struct copying <b>might</b> be optimized to current the entire struct in one go.
	 */
	public boolean isSequential() {
		return this.type.isSequential(this.data);
	}

	public boolean canSingleOpCopy(Struct dst) {
		return dst.isSequential() && this.isSequential() && (dst.getIndex() >= 0) == (this.getIndex() >= 0);
	}

	protected <U extends GlValue<End> & GlValue.Uniform & GlValue.Copiable> U field(GlValue.Type<U> type) {
		if(this.fields != null) {
			this.fields.add(type);
		}

		U u = type.create(this.data, null);
		this.uniforms.add(u);
		return u;
	}

	private void copyTo(Struct dst) {
		if(this.canSingleOpCopy(dst)) {
			GlData fromData = this.data.getSelf(), toData = dst.data.getSelf();
			if(fromData instanceof UniformData fromU && toData instanceof UniformData toU) {
				StructTypeImpl.FactoryImpl a = this.type.factory, b = dst.type.factory;
				fromU.copyTo(toU, a.group, a.off, this.getIndex(), b.group, b.off, dst.getIndex(), a.len);
			} else {
				throw new UnsupportedOperationException("unrecognized current " + toData.getClass() + " to " + fromData.getClass());
			}
		} else {
			for(int i = 0; i < dst.uniforms.size(); i++) {
				GlValue<?> from = this.uniforms.get(i), to = dst.uniforms.get(i);
				ShaderImpl.copyUniform_(from, to);
			}
		}
	}

	public interface TypeFactory<T extends Struct> {
		GlValue.Type<T> named(String name);
	}

	public interface Initializer<T extends Struct> {
		T init(GlData data, GlValue<?> next, String structName);
	}
}
