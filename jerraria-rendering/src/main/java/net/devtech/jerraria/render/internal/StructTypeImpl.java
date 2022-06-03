package net.devtech.jerraria.render.internal;

import java.util.HashSet;
import java.util.Set;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.api.Struct;
import net.devtech.jerraria.render.api.basic.GlData;

public final class StructTypeImpl<T extends Struct> extends GlValue.Type<T> {
	public final FactoryImpl<T> factory;
	final DummyGlData dummy = new DummyGlData();
	final String name;

	public static class FactoryImpl<T extends Struct> implements Struct.TypeFactory<T> {
		final Struct.Initializer<T> initializer;
		final Set<String> names = new HashSet<>();
		public int off = -1, len, group, array;

		public FactoryImpl(Struct.Initializer<T> initializer) {
			this.initializer = initializer;
		}

		public boolean isSequential(GlData data) {
			if(this.off == -1) {
				boolean isSeq = true;
				int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, group = -1, array = -3;
				// check group index
				for(String name : this.names) {
					GlData.Element self = data.getSelf().getElement(name).getSelf();
					if(self instanceof ElementImpl e) {
						int off = e.byteOffset();
						min = Math.min(min, off);
						max = Math.max(max, off + e.type().byteCount);
						if(group != -1 && e.groupIndex() != group) {
							isSeq = false;
							break;
						}
						if(array != -3 && e.arrayIndex() != array) {
							isSeq = false;
							break;
						}
						group = e.groupIndex();
						array = e.arrayIndex();
					} else if(self != null) {
						isSeq = false;
						break;
					}
				}

				if(isSeq) {
					UniformData self = (UniformData) data.getSelf();
					for(GlData.Element value : self.elements.values()) {
						if(value instanceof ElementImpl e) {
							if(!this.names.contains(e.name()) && e.byteOffset() >= min && e.byteOffset() <= max && (e.arrayIndex() >= 0) == (array >= 0)) {
								isSeq = false;
								break;
							}
						}
					}
				}

				if(isSeq) {
					this.off = min;
					this.len = max - min;
					this.group = group;
					this.array = array;
					return true;
				} else {
					this.off = -2;
					return false;
				}
			}
			return this.off != -2;
		}

		@Override
		public GlValue.Type<T> named(String name) {
			return new StructTypeImpl<>(this, name);
		}
	}

	public StructTypeImpl(FactoryImpl<T> initializer, String name) {
		this.factory = initializer;
		this.name = name;
	}

	public boolean isSequential(GlData data) {
		return this.factory.isSequential(data);
	}

	@Override
	public T create(GlData data, GlValue<?> next) {
		T init = this.factory.initializer.init(data, next, this.name);
		ShaderImpl.set(init, this);
		return init;
	}

	@Override
	public void attach(BareShader.Uncompiled uncompiled, GlValue.Loc isUniform) {
		T init = this.factory.initializer.init(this.dummy, null, this.name);
		for(GlValue.Type<?> field : ShaderImpl.fields(init)) {
			field.attach(uncompiled, isUniform);
		}
	}

	private static final GlData.BufAdapter ADAPTER = new GlData.BufAdapter() {};
	private static final GlData.Element ELEMENT = new GlData.Element() {};
	public final class DummyGlData extends GlData {
		@Override
		public Buf element(Element element) {
			return ADAPTER;
		}

		@Override
		public Element getElement(String name) {
			StructTypeImpl.this.factory.names.add(name);
			return ELEMENT;
		}

		@Override
		protected void invalidate() throws Exception {
		}
	}
}
