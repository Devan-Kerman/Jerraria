package net.devtech.jerraria.util.math;

import net.devtech.jerraria.render.api.base.GlData;

public final class Matrix3f extends Mat implements AutoCloseable {
	private float a11, a12, a13, a21, a22, a23, a31, a32, a33;

	public Matrix3f() {
		this.a33 = this.a22 = this.a11 = 1;
	}

	public Matrix3f identity() {
		this.a12 = this.a13 = this.a21 = this.a23 = this.a31 = this.a32 = 0;
		this.a33 = this.a22 = this.a11 = 1;
		return this;
	}

	@Override
	public float mulX(float x, float y, float z) {
		return a11 * x + a12 * y + a13 * z;
	}

	@Override
	public float mulY(float x, float y, float z) {
		return a21 * x + a22 * y + a23 * z;
	}

	@Override
	public float mulZ(float x, float y, float z) {
		return a31 * x + a32 * y + a33 * z;
	}

	@Override
	public Matrix3f offset(float x, float y, float z) {
		this.offset(x, y);
		return this;
	}

	@Override
	public float mulX(float x, float y, float z, float w) {
		return this.mulX(x, y, z);
	}

	@Override
	public float mulY(float x, float y, float z, float w) {
		return this.mulY(x, y, z);
	}

	@Override
	public float mulZ(float x, float y, float z, float w) {
		return this.mulZ(x, y, z);
	}

	@Override
	public float mulW(float x, float y, float z, float w) {
		return w;
	}

	@Override
	public Matrix3f offset(float x, float y) {
		this.a13 = this.a11 * x + this.a12 * y + this.a13;
		this.a23 = this.a21 * x + this.a22 * y + this.a23;
		this.a33 = this.a31 * x + this.a32 * y + this.a33;
		return this;
	}

	@Override
	public Matrix3f scale(float scaleX, float scaleY) {
		a11 *= scaleX;
		a21 *= scaleX;
		a31 *= scaleX;

		a12 *= scaleY;
		a22 *= scaleY;
		a32 *= scaleY;
		return this;
	}

	@Override
	public Mat scale(float x, float y, float z) {
		return this.scale(x, y);
	}

	public Matrix3f offsetInverse(float x, float y) {
		a11 = a11 + x * a31;
		a12 = a12 + x * a32;
		a13 = a13 + x * a33;

		a21 = a21 + y * a31;
		a22 = a22 + y * a32;
		a23 = a23 + y * a33;
		return this;
	}

	public Matrix3f scaleInverse(float scaleX, float scaleY) {
		a11 *= scaleX;
		a12 *= scaleX;
		a13 *= scaleX;

		a21 *= scaleY;
		a22 *= scaleY;
		a23 *= scaleY;
		return this;
	}

	@Override
	public Matrix3f copy() {
		Matrix3f mat = new Matrix3f();
		mat.a11 = this.a11;
		mat.a12 = this.a12;
		mat.a13 = this.a13;

		mat.a21 = this.a21;
		mat.a22 = this.a22;
		mat.a23 = this.a23;

		mat.a31 = this.a31;
		mat.a32 = this.a32;
		mat.a33 = this.a33;
		return mat;
	}

	@Override
	public Matrix3f load(Mat from) {
		if(from instanceof Matrix3f m) {
			return this.load(m);
		} else {
			this.a11 = from.getElement(0, 0);
			this.a12 = from.getElement(0, 1);
			this.a13 = from.getElement(0, 2);

			this.a21 = from.getElement(1, 0);
			this.a22 = from.getElement(1, 1);
			this.a23 = from.getElement(1, 2);

			this.a31 = from.getElement(2, 0);
			this.a32 = from.getElement(2, 1);
			this.a33 = from.getElement(2, 2);
			return this;
		}
	}

	@Override
	public float getElement(int x, int y) {
		return switch(x) {
			case 0 -> switch(y) {
				case 0 -> this.a11;
				case 1 -> this.a12;
				case 2 -> this.a13;
				default -> 0;
			};

			case 1 -> switch(y) {
				case 0 -> this.a21;
				case 1 -> this.a22;
				case 2 -> this.a23;
				default -> 0;
			};

			case 2 -> switch(y) {
				case 0 -> this.a31;
				case 1 -> this.a32;
				case 2 -> this.a33;
				default -> 0;
			};

			default -> x == y ? 1 : 0;
		};
	}


	public Matrix3f load(Matrix3f mat) {
		this.a11 = mat.a11;
		this.a12 = mat.a12;
		this.a13 = mat.a13;
		this.a21 = mat.a21;
		this.a22 = mat.a22;
		this.a23 = mat.a23;
		this.a31 = mat.a31;
		this.a32 = mat.a32;
		this.a33 = mat.a33;
		return this;
	}

	@Override
	public void upload3x3(GlData.Buf buf) {
		buf.f(this.a11).f(this.a21).f(this.a31)
		   .f(this.a12).f(this.a22).f(this.a32)
		   .f(this.a13).f(this.a23).f(this.a33);
	}

	@Override
	public void upload4x4(GlData.Buf buf) {
		buf.f(this.a11).f(this.a21).f(this.a31).f(0);
		buf.f(this.a12).f(this.a22).f(this.a32).f(0);
		buf.f(this.a13).f(this.a23).f(this.a33).f(0);
		buf.f(0)       .f(0)       .f(0)       .f(1);
	}

	@Override
	public void close() {

	}
}
