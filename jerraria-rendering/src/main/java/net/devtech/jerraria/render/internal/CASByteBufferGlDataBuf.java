package net.devtech.jerraria.render.internal;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.basic.GlData;

public abstract class CASByteBufferGlDataBuf extends ByteBufferGlDataBuf {
	protected int pos;

	@Override
	public GlData.Buf f(float f) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.putFloat(pos, f);
			this.pos = pos + 4;
		} while(buffer != this.getBuffer());
		return this;
	}

	@Override
	public GlData.Buf i(int i) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.putInt(pos, i);
			this.pos = pos + 4;
		} while(buffer != this.getBuffer());
		return this;
	}

	@Override
	public GlData.Buf b(byte b) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.put(pos, b);
			this.pos = pos + 1;
		} while(buffer != this.getBuffer());
		return this;
	}

	@Override
	public GlData.Buf bool(boolean b) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.put(pos, (byte) (b ? 1 : 0));
			this.pos = pos + 1;
		} while(buffer != this.getBuffer());
		return this;
	}

	@Override
	public GlData.Buf s(short s) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.putShort(pos, s);
			this.pos = pos + 2;
		} while(buffer != this.getBuffer());
		return this;
	}

	@Override
	public GlData.Buf c(char c) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.putChar(pos, c);
			this.pos = pos + 2;
		} while(buffer != this.getBuffer());
		return this;
	}

	@Override
	public GlData.Buf d(double d) {
		ByteBuffer buffer;
		do {
			buffer = this.getBuffer();
			int pos = this.pos;
			buffer.putDouble(pos, d);
			this.pos = pos + 8;
		} while(buffer != this.getBuffer());
		return this;
	}
}
