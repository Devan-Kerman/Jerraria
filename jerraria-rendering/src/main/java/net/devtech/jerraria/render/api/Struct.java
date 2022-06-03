package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.internal.StructTypeImpl;
import net.devtech.jerraria.render.internal.UniformData;

public abstract class Struct extends GlValue<End> implements GlValue.Uniform, GlValue.Copiable {
	protected final String structName;
	final List<GlValue.Type<?>> fields;
	final List<GlValue<?>> uniforms = new ArrayList<>();
	StructTypeImpl<?> type;

	protected Struct(GlData data, GlValue next, String structName) {
		super(data, next);
		this.fields = data instanceof StructTypeImpl.DummyGlData ? new ArrayList<>() : null;
		this.structName = structName;
	}

	/**
	 * The {@link Struct}'s analog for {@link GlValue#simple(SimpleType, DataType, String, String)}
	 * @implSpec Stores common data for this struct, this basically stores whether the struct can be efficiently copied and how to do so.
	 * @see TypeFactory#named(String)
	 */
	public static <T extends Struct> TypeFactory<T> factory(Initializer<T> initializer) {
		return new StructTypeImpl.FactoryImpl<>(initializer);
	}

	@Override
	public void copyTo(GlValue value) {
		this.copyTo((Struct) value);
	}

	protected <U extends GlValue<End> & GlValue.Uniform & GlValue.Copiable> U field(GlValue.Type<U> type) {
		if(this.fields != null) {
			this.fields.add(type);
		}

		U u = type.create(this.data, null);
		this.uniforms.add(u);
		return u;
	}

	private void copyTo(Struct struct) {
		if(this.type.isSequential(this.data) && struct.type.isSequential(struct.data)) {
			GlData fromData = this.data.getSelf(), toData = struct.data.getSelf();
			if(fromData instanceof UniformData fromU && toData instanceof UniformData toU) {
				StructTypeImpl.FactoryImpl a = this.type.factory, b = struct.type.factory;
				fromU.copyTo(
					toU,
					a.group,
					a.off,
					a.array,
					b.group,
					b.off,
					b.array,
					a.len
				);
			} else {
				throw new UnsupportedOperationException("unrecognized copy " + toData.getClass() + " to " + fromData.getClass());
			}
		} else {
			for(int i = 0; i < struct.uniforms.size(); i++) {
				GlValue<?> from = this.uniforms.get(i), to = struct.uniforms.get(i);
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
