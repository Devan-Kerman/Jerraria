package net.devtech.jerraria.util.math;

public class Matrix3f implements AutoCloseable {
	private float a11, a12, a13, a21, a22, a23, a31, a32, a33;

	public Matrix3f() {
		this.a33 = this.a22 = this.a11 = 1;
	}

	public Matrix3f identity() {
		this.a12 = this.a13 = this.a21 = this.a23 = this.a31 = this.a32 = 0;
		this.a33 = this.a22 = this.a11 = 1;
		return this;
	}

	public float mulX(float x, float y, float z) {
		return a11 * x + a12 * y + a13 * z;
	}

	public float mulY(float x, float y, float z) {
		return a21 * x + a22 * y + a23 * z;
	}

	public float mulZ(float x, float y, float z) {
		return a31 * x + a32 * y + a33 * z;
	}

	public Matrix3f offset(float x, float y) {
		this.a13 = this.a11 * x + this.a12 * y + this.a13;
		this.a23 = this.a21 * x + this.a22 * y + this.a23;
		this.a33 = this.a31 * x + this.a32 * y + this.a33;
		return this;
	}

	public Matrix3f scale(float scaleX, float scaleY) {
		a11 *= scaleX;
		a21 *= scaleX;
		a31 *= scaleX;

		a12 *= scaleY;
		a22 *= scaleY;
		a32 *= scaleY;
		return this;
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
	public void close() {

	}
}