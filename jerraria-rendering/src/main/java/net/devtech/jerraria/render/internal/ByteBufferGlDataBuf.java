package net.devtech.jerraria.render.internal;

import java.nio.ByteBuffer;

public abstract class ByteBufferGlDataBuf implements GlData.Buf {
	protected abstract ByteBuffer getBuffer();

	@Override
	public GlData.Buf f(float f) {
		this.getBuffer().putFloat(f);
		return this;
	}

	@Override
	public GlData.Buf i(int i) {
		this.getBuffer().putInt(i);
		return this;
	}

	@Override
	public GlData.Buf b(byte b) {
		this.getBuffer().put(b);
		return this;
	}

	@Override
	public GlData.Buf bool(boolean b) {
		this.getBuffer().put((byte) (b ? 1 : 0));
		return this;
	}

	@Override
	public GlData.Buf s(short s) {
		this.getBuffer().putShort(s);
		return this;
	}

	@Override
	public GlData.Buf c(char c) {
		this.getBuffer().putChar(c);
		return this;
	}

	@Override
	public GlData.Buf d(double d) {
		this.getBuffer().putDouble(d);
		return this;
	}
}
