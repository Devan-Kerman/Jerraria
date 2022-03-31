package net.devtech.jerraria.render.math;

public class Matrix3f {
	private float a11, a12, a13, a21, a22, a23, a31, a32, a33;

	public Matrix3f() {
		a33 = a22 = a11 = 1;
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

	public Matrix3f translate(float x, float y) {
		a11 = a11 + x * a31;
		a12 = a12 + x * a32;
		a13 = a13 + x * a33;

		a21 = a21 + y * a31;
		a22 = a21 + y * a32;
		a23 = a21 + y * a33;
		return this;
	}

	public Matrix3f scale(float scaleX, float scaleY) {
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

	public static void main(String[] args) {
		Matrix3f mat2 = new Matrix3f();
		mat2.scale(2, 2);
		mat2.translate(-1, -1);

		System.out.println(mat2.mulX(1, 1, 1) + " " + mat2.mulY(1, 1, 1));
	}
}
