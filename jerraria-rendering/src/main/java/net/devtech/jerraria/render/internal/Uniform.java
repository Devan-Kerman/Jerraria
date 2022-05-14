package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.glUniform1f;
import static org.lwjgl.opengl.GL31.glUniform1i;
import static org.lwjgl.opengl.GL31.glUniform2f;
import static org.lwjgl.opengl.GL31.glUniform2i;
import static org.lwjgl.opengl.GL31.glUniform3f;
import static org.lwjgl.opengl.GL31.glUniform3i;
import static org.lwjgl.opengl.GL31.glUniform4f;
import static org.lwjgl.opengl.GL31.glUniform4i;
import static org.lwjgl.opengl.GL31.glUniformMatrix2fv;
import static org.lwjgl.opengl.GL31.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL31.glUniformMatrix4fv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import org.lwjgl.opengl.GL13;

/**
 * Non buffer object uniform data
 * todo implement equals method to compare Shader objects
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
		Uniform copy = createNew(uniform);
		uniform.copyTo(copy);
		copy.rebind = true;
		return copy;
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

	abstract void copyTo(Uniform uniform);

	abstract void reset();

	abstract void upload();

	void alwaysUpload() {}

	static class Matrix extends Uniform {
		final FloatBuffer buf;

		protected Matrix(DataType type, int location) {
			super(type, location);
			this.buf = ByteBuffer.allocateDirect(type.byteCount).order(ByteOrder.nativeOrder()).asFloatBuffer();
		}

		@Override
		public GlData.Buf f(float f) {
			this.buf.put(f);
			return this;
		}

		@Override
		void copyTo(Uniform uniform) {
			Matrix matrix = (Matrix) uniform;
			matrix.buf.put(this.buf);
		}

		@Override
		void reset() {
			this.buf.position(0);
		}

		@Override
		void upload() {
			this.reset();
			switch(this.type.elementCount) {
				case 4 -> glUniformMatrix2fv(this.location, false, this.buf);
				case 9 -> glUniformMatrix3fv(this.location, false, this.buf);
				case 16 -> glUniformMatrix4fv(this.location, false, this.buf);
				default -> throw new UnsupportedOperationException("Unsupported matrix size " + this.type.elementCount);
			}
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
		void upload() {
			glUniform1i(this.location, this.textureUnit);
		}

		@Override
		void copyTo(Uniform uniform) {
			Sampler texture = (Sampler) uniform;
			texture.textureId = this.textureId;
		}

		@Override
		void alwaysUpload() {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + this.textureUnit);
			GL13.glBindTexture(this.type.elementType, this.textureId);
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
		void copyTo(Uniform uniform) {
			Int copy = (Int) uniform;
			copy.index = this.index;
			copy.a = this.a;
			copy.b = this.b;
			copy.c = this.c;
			copy.d = this.d;
		}

		@Override
		void reset() {
			this.index = 0;
		}

		@Override
		void upload() {
			switch(this.type.elementCount) {
				case 1 -> glUniform1i(this.location, this.a);
				case 2 -> glUniform2i(this.location, this.a, this.b);
				case 3 -> glUniform3i(this.location, this.a, this.b, this.c);
				case 4 -> glUniform4i(this.location, this.a, this.b, this.c, this.d);
			}
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
		void upload() {
			switch(this.type.elementCount) {
				case 1 -> glUniform1f(this.location, this.a);
				case 2 -> glUniform2f(this.location, this.a, this.b);
				case 3 -> glUniform3f(this.location, this.a, this.b, this.c);
				case 4 -> glUniform4f(this.location, this.a, this.b, this.c, this.d);
			}
		}

		@Override
		void copyTo(Uniform uniform) {
			Float copy = (Float) uniform;
			copy.index = this.index;
			copy.a = this.a;
			copy.b = this.b;
			copy.c = this.c;
			copy.d = this.d;
		}
	}
}
