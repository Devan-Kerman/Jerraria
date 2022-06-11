package net.devtech.jerraria.render.internal;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.base.GlData;

public abstract class ConcurrentByteBufferGlDataBuf extends ByteBufferGlDataBuf {
	protected int pos;

	@Override
	public GlData.Buf f(float f) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.putFloat(pos, f);
		} while(buffer != this.getBuffer());
		this.pos = pos + 4;
		return this;
	}

	@Override
	public GlData.Buf i(int i) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.putInt(pos, i);
		} while(buffer != this.getBuffer());
		this.pos = pos + 4;
		return this;
	}

	@Override
	public GlData.Buf b(byte b) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.put(pos, b);
		} while(buffer != this.getBuffer());
		this.pos = pos + 1;
		return this;
	}

	@Override
	public GlData.Buf bool(boolean b) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.put(pos, (byte) (b ? 1 : 0));
		} while(buffer != this.getBuffer());
		this.pos = pos + 1;
		return this;
	}

	@Override
	public GlData.Buf s(short s) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.putShort(pos, s);
		} while(buffer != this.getBuffer());
		this.pos = pos + 2;
		return this;
	}

	@Override
	public GlData.Buf c(char c) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.putChar(pos, c);
		} while(buffer != this.getBuffer());
		this.pos = pos + 2;
		return this;
	}

	@Override
	public GlData.Buf d(double d) {
		ByteBuffer buffer;
		int pos = this.pos;
		do {
			buffer = this.getBuffer();
			buffer.putDouble(pos, d);
		} while(buffer != this.getBuffer());
		this.pos = pos + 8;
		return this;
	}
}
