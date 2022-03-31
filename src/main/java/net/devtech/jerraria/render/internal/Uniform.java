package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform2i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform3i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniform4i;
import static org.lwjgl.opengl.GL20.glUniformMatrix2fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL13;

/**
 * Non buffer object uniform data
 */
public abstract class Uniform implements GlData.Buf {
	final DataType type;
	final int location;
	boolean rebind;

	protected Uniform(DataType type, int location) {
		this.type = type;
		this.location = location;
	}

	public static Uniform createSampler(DataType type, int location, int textureUnit) {
		return new Sampler(type, location, textureUnit);
	}

	public static Uniform create(DataType type, int location) {
		if(type.isMatrix) {
			return new Matrix(type, location);
		} else if(type.isFloating()) {
			return new Float(type, location);
		} else {
			return new Int(type, location);
		}
	}

	public static Uniform createNew(Uniform uniform) {
		if(uniform instanceof Sampler s) {
			return new Sampler(s.type, s.location, s.textureUnit);
		}
		return create(uniform.type, uniform.location);
	}

	public static Uniform copy(Uniform uniform) {
		return uniform.copy();
	}

	@Override
	public GlData.Buf f(float f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GlData.Buf i(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GlData.Buf b(byte b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GlData.Buf bool(boolean b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GlData.Buf s(short s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GlData.Buf c(char c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GlData.Buf d(double d) {
		throw new UnsupportedOperationException();
	}

	abstract void reset();

	abstract void bind();

	abstract Uniform copy();

	static class Matrix extends Uniform {
		final FloatBuffer buf;

		protected Matrix(DataType type, int location) {
			super(type, location);
			this.buf = FloatBuffer.allocate(type.byteCount / 4);
		}

		@Override
		public GlData.Buf f(float f) {
			this.buf.put(f);
			return this;
		}

		@Override
		void reset() {
			this.buf.position(0);
		}

		@Override
		void bind() {
			this.reset();
			switch(this.type.elementCount) {
				case 4 -> glUniformMatrix2fv(this.location, false, this.buf);
				case 9 -> glUniformMatrix3fv(this.location, false, this.buf);
				case 16 -> glUniformMatrix4fv(this.location, false, this.buf);
				default -> throw new UnsupportedOperationException("Unsupported matrix size " + this.type.elementCount);
			}
		}

		@Override
		Uniform copy() {
			Matrix matrix = new Matrix(this.type, this.location);
			matrix.buf.put(this.buf);
			return matrix;
		}
	}

	static class Sampler extends Uniform {
		final int textureUnit;
		int textureId;

		protected Sampler(DataType type, int location, int unit) {
			super(type, location);
			this.textureUnit = unit;
		}

		@Override
		public GlData.Buf i(int i) {
			this.textureId = i;
			return this;
		}

		@Override
		void reset() {
			this.textureId = -1;
		}

		@Override
		void bind() {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + this.textureUnit);
			GL13.glBindTexture(this.type.elementType, this.textureId);
			glUniform1i(this.location, this.textureUnit);
		}

		@Override
		Uniform copy() {
			Sampler texture = new Sampler(this.type, this.location, this.textureUnit);
			texture.textureId = this.textureId;
			return texture;
		}
	}

	static class Int extends Uniform {
		byte index;
		int a, b, c, d;

		protected Int(DataType type, int location) {
			super(type, location);
		}

		@Override
		public GlData.Buf i(int i) {
			switch(this.index++) {
				case 0 -> this.a = i;
				case 1 -> this.b = i;
				case 2 -> this.c = i;
				case 3 -> this.d = i;
				default -> throw new IndexOutOfBoundsException(this.index - 1);
			}
			return this;
		}

		@Override
		public GlData.Buf b(byte b) {
			return this.i(b);
		}

		@Override
		public GlData.Buf bool(boolean b) {
			return this.i(b ? 1 : 0);
		}

		@Override
		public GlData.Buf s(short s) {
			return this.i(s);
		}

		@Override
		public GlData.Buf c(char c) {
			return this.i(c);
		}

		@Override
		void reset() {
			this.index = 0;
		}

		@Override
		void bind() {
			if(this.rebind) {
				switch(this.type.elementCount) {
					case 1 -> glUniform1i(this.location, this.a);
					case 2 -> glUniform2i(this.location, this.a, this.b);
					case 3 -> glUniform3i(this.location, this.a, this.b, this.c);
					case 4 -> glUniform4i(this.location, this.a, this.b, this.c, this.d);
				}
				this.rebind = false;
			}
		}

		@Override
		Uniform copy() {
			Int copy = new Int(this.type, this.location);
			copy.index = this.index;
			copy.a = this.a;
			copy.b = this.b;
			copy.c = this.c;
			copy.d = this.d;
			return copy;
		}
	}

	static class Float extends Uniform {
		byte index;
		float a, b, c, d;

		protected Float(DataType type, int location) {
			super(type, location);
		}

		@Override
		public GlData.Buf f(float i) {
			switch(this.index++) {
				case 0 -> this.a = i;
				case 1 -> this.b = i;
				case 2 -> this.c = i;
				case 3 -> this.d = i;
				default -> throw new IndexOutOfBoundsException(this.index - 1);
			}
			return this;
		}

		@Override
		void reset() {
			this.index = 0;
		}

		@Override
		void bind() {
			switch(this.type.elementCount) {
				case 1 -> glUniform1f(this.location, this.a);
				case 2 -> glUniform2f(this.location, this.a, this.b);
				case 3 -> glUniform3f(this.location, this.a, this.b, this.c);
				case 4 -> glUniform4f(this.location, this.a, this.b, this.c, this.d);
			}
		}

		@Override
		Uniform copy() {
			Float copy = new Float(this.type, this.location);
			copy.index = this.index;
			copy.a = this.a;
			copy.b = this.b;
			copy.c = this.c;
			copy.d = this.d;
			return copy;
		}
	}
}
