package net.devtech.jerraria.render;

import net.devtech.jerraria.render.api.base.GlData;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class VertexConsumerGlData extends GlData {
	final MinecraftShader<?> shader;
	final VertexConsumer consumer;
	final MinecraftBuf[] cache;
	MinecraftBuf last;

	public VertexConsumerGlData(MinecraftShader<?> shader, VertexConsumer consumer) {
		this.shader = shader;
		this.consumer = consumer;
		this.cache = new MinecraftBuf[shader.format.getAttributeNames().size()];
	}

	@Override
	public Buf element(Element element) {
		this.flush();
		MinecraftShader.ElementImpl impl = (MinecraftShader.ElementImpl) element;
		int index = impl.index();
		MinecraftBuf buf = this.cache[index];
		if(buf == null) {
			VertexFormatElement element1 = impl.element();
			if(element1 == VertexFormats.POSITION_ELEMENT) {
				buf = new PosBuf();
			} else if(element1 == VertexFormats.COLOR_ELEMENT) {
				buf = new ColorBuf();
			} else if(element1 == VertexFormats.TEXTURE_ELEMENT) {
				buf = new TexBuf();
			} else if(element1 == VertexFormats.OVERLAY_ELEMENT) {
				buf = new OverlayBuf();
			} else if(element1 == VertexFormats.LIGHT_ELEMENT) {
				return new LightBuf();
			} else if(element1 == VertexFormats.NORMAL_ELEMENT) {
				return new Normal();
			} else {
				throw new UnsupportedOperationException();
			}

			this.cache[index] = buf;
		}
		return buf;
	}

	@Override
	public Element getElement(String name) {
		return this.shader.elements.get(name);
	}

	private void flush() {
		if(this.last != null) {
			this.last.flush();
			this.last = null;
		}
	}

	public void next() {
		this.flush();
		this.consumer.next();
	}

	interface MinecraftBuf extends BufAdapter {
		void flush();
	}

	@Override
	protected void invalidate() throws Exception {
		throw new UnsupportedOperationException();
	}

	class ColorBuf implements MinecraftBuf {
		int argb;
		byte counter;

		@Override
		public Buf b(byte b) {
			this.argb |= ((b & 0xFF) << this.counter++);
			return this;
		}

		@Override
		public void flush() {
			VertexConsumerGlData.this.consumer.color(this.argb);
			this.counter = 0;
		}
	}

	class PosBuf implements MinecraftBuf {
		float a, b, c;
		byte counter;

		@Override
		public Buf f(float f) {
			switch(this.counter++) {
				case 0 -> this.a = f;
				case 1 -> this.b = f;
				case 2 -> this.c = f;
			}
			return this;
		}

		@Override
		public void flush() {
			VertexConsumerGlData.this.consumer.vertex(this.a, this.b, this.c);
			this.counter = 0;
		}
	}

	class TexBuf implements MinecraftBuf {
		float a, b;
		byte counter;

		@Override
		public Buf f(float f) {
			switch(this.counter++) {
				case 0 -> this.a = f;
				case 1 -> this.b = f;
			}
			return this;
		}

		@Override
		public void flush() {
			VertexConsumerGlData.this.consumer.texture(this.a, this.b);
			this.counter = 0;
		}
	}

	class OverlayBuf implements MinecraftBuf {
		short a, b;
		byte counter;

		@Override
		public Buf s(short s) {
			switch(this.counter++) {
				case 0 -> this.a = s;
				case 1 -> this.b = s;
			}
			return this;
		}

		@Override
		public void flush() {
			VertexConsumerGlData.this.consumer.overlay(this.a, this.b);
			this.counter = 0;
		}
	}

	class LightBuf extends OverlayBuf {
		@Override
		public void flush() {
			VertexConsumerGlData.this.consumer.light(this.a, this.b);
			this.counter = 0;
		}
	}

	class Normal implements MinecraftBuf {
		byte a, b, c;
		byte counter;

		@Override
		public Buf b(byte b) {
			switch(this.counter++) {
				case 0 -> this.a = b;
				case 1 -> this.b = b;
				case 2 -> this.c = b;
			}
			return this;
		}

		@Override
		public void flush() {
			VertexConsumerGlData.this.consumer.normal(this.a/255f, this.b/255f, this.c/255f);
			this.counter = 0;
		}
	}
}
