package net.devtech.jerraria.render.internal.arr;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderBuffer;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.UniformData;

public class ShaderBufferImpl<T extends GlValue<?> & GlValue.Uniform> implements ShaderBuffer<T>, Int2ObjectFunction<T> {
	final GlData uniforms;
	final String name;
	final Int2ObjectMap<T> cache = new Int2ObjectOpenHashMap<>();
	final Shader.BufferFunction<GlValue.Type<T>> operator;
	final int maxSize;
	int current;

	public ShaderBufferImpl(GlData uniforms, String elementName, Shader.BufferFunction<GlValue.Type<T>> operator, int size) {
		this.name = elementName;
		this.operator = operator;
		this.maxSize = size;
		this.uniforms = uniforms;
	}

	public class ArrayGlValue extends GlValue.Type<T> {
		@Override
		public T create(GlData data, GlValue<?> next) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void attach(BareShader.Uncompiled uncompiled, GlValue.Loc isUniform) {
			ShaderBufferImpl.this.operator.apply(String.format(ShaderBufferImpl.this.name, 0)).attach(uncompiled, isUniform);
		}
	}

	@Override
	public T getOrCreate(int index) {
		if(index >= this.maxSize) {
			throw new IndexOutOfBoundsException(index + " >= " + this.maxSize);
		}
		this.current = Math.max(index, this.current);
		return this.cache.computeIfAbsent(index, this);
	}

	@Override
	public int getCurrentSize() {
		return this.current;
	}

	@Override
	public T get(int key) {
		UniformData uniforms = (UniformData) this.uniforms.getSelf();
		GlValue.Type<T> apply = this.operator.apply(String.format(ShaderBufferImpl.this.name, key));
		apply.validateUniform();
		GlData custom = new GlData() {
			@Override
			public Buf element(Element element) {
				return uniforms.element(element);
			}

			@Override
			public Element getElement(String name) {
				String end = "[" + key + "]";
				int last = name.lastIndexOf(end);
				String origin = name.substring(0, last) + "[0]" + name.substring(last+end.length());
				return uniforms.getElement(uniforms.getElement(origin), key);
			}

			@Override
			protected void invalidate() {}

			@Override
			public GlData getSelf() {
				return uniforms;
			}
		};
		return apply.create(custom, null);
	}
}
