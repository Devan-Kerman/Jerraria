package net.devtech.jerraria.util.math;

import it.unimi.dsi.fastutil.floats.FloatStack;
import net.devtech.jerraria.render.api.base.GlData;

public final class Mat3f extends Mat implements AutoCloseable {
	/**
	 * [a11 a12 a13]
	 * [a21 a22 a23]
	 * [a31 a32 a33]
	 */
	private float a11, a12, a13, a21, a22, a23, a31, a32, a33;

	public Mat3f inverse() {
		Mat3f inverted = new Mat3f();
		float a11 = this.a11, a12 = this.a12, a13 = this.a13;
		float a21 = this.a21, a22 = this.a22, a23 = this.a23;
		float a31 = this.a31, a32 = this.a32, a33 = this.a33;

		float clean2w1 = a21 / a11;
		a21 -= a11 * clean2w1;
		a22 -= a12 * clean2w1;
		a23 -= a13 * clean2w1;
		inverted.a21 -= inverted.a11 * clean2w1;
		inverted.a22 -= inverted.a12 * clean2w1;
		inverted.a23 -= inverted.a13 * clean2w1;

		float clean3w1 = a31 / a11;
		a31 -= a11 * clean3w1;
		a32 -= a12 * clean3w1;
		a33 -= a13 * clean3w1;
		inverted.a31 -= inverted.a11 * clean3w1;
		inverted.a32 -= inverted.a12 * clean3w1;
		inverted.a33 -= inverted.a13 * clean3w1;

		float clean3w2 = a32 / a22;
		a31 -= a21 * clean3w2;
		a32 -= a22 * clean3w2;
		a33 -= a23 * clean3w2;
		inverted.a31 -= inverted.a21 * clean3w2;
		inverted.a32 -= inverted.a22 * clean3w2;
		inverted.a33 -= inverted.a23 * clean3w2;

		float clean2w3 = a23 / a33;
		a21 -= a31 * clean2w3;
		a22 -= a32 * clean2w3;
		inverted.a21 -= inverted.a31 * clean2w3;
		inverted.a22 -= inverted.a32 * clean2w3;
		inverted.a23 -= inverted.a33 * clean2w3;

		float clean1w3 = a13 / a33;
		a11 -= a31 * clean1w3;
		a12 -= a32 * clean1w3;
		inverted.a11 -= inverted.a31 * clean1w3;
		inverted.a12 -= inverted.a32 * clean1w3;
		inverted.a13 -= inverted.a33 * clean1w3;

		float clean1w2 = a12 / a22;
		a11 -= a21 * clean1w2;
		inverted.a11 -= inverted.a21 * clean1w2;
		inverted.a12 -= inverted.a22 * clean1w2;
		inverted.a13 -= inverted.a23 * clean1w2;

		inverted.a11 /= a11;
		inverted.a12 /= a11;
		inverted.a13 /= a11;

		inverted.a21 /= a22;
		inverted.a22 /= a22;
		inverted.a23 /= a22;

		inverted.a31 /= a33;
		inverted.a32 /= a33;
		inverted.a33 /= a33;

		return inverted;
	}

	public Mat3f() {
		this.a33 = this.a22 = this.a11 = 1;
	}

	public Mat3f identity() {
		this.a12 = this.a13 = this.a21 = this.a23 = this.a31 = this.a32 = 0;
		this.a33 = this.a22 = this.a11 = 1;
		return this;
	}

	@Override
	public float mulX(float x, float y, float z) {
		return this.a11 * x + this.a12 * y + this.a13 * z;
	}

	@Override
	public float mulY(float x, float y, float z) {
		return this.a21 * x + this.a22 * y + this.a23 * z;
	}

	@Override
	public float mulZ(float x, float y, float z) {
		return this.a31 * x + this.a32 * y + this.a33 * z;
	}

	@Override
	public Mat3f offset(float x, float y, float z) {
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
	public Mat3f offset(float x, float y) {
		this.a13 = this.a11 * x + this.a12 * y + this.a13;
		this.a23 = this.a21 * x + this.a22 * y + this.a23;
		this.a33 = this.a31 * x + this.a32 * y + this.a33;
		return this;
	}

	@Override
	public Mat rotate(float rad) {
		float sin = JMath.sin(rad);
		float cos = JMath.cos(rad);

		float a11 = this.a11;
		this.a11 = a11 * cos + this.a12 * sin;
		this.a12 = a11 * -sin + this.a12 * cos;

		float a21 = this.a21;
		this.a21 = a21 * cos + this.a22 * sin;
		this.a22 = a21 * -sin + this.a22 * cos;

		float a31 = this.a31;
		this.a31 = a31 * cos + this.a32 * sin;
		this.a32 = a31 * -sin + this.a32 * cos;
		return this;
	}

	public static void main(String[] args) {
		float rad = (float) Math.toRadians(45);

		Mat3f mat = new Mat3f();
		mat.scale(10, 10);
		mat.offset(10, 10);
		mat.rotate(rad);
		mat.offset(10, 10);

		float x = mat.mulX(1, 0);
		float y = mat.mulY(1, 0);
		System.out.println(x + " " + y);

		Mat3f inverse = mat.inverse();
		System.out.println(inverse.mulX(x, y) + " " + inverse.mulY(x, y));
	}

	@Override
	public Mat rotate(float radX, float radY, float radZ) {
		return this.rotate(radZ);
	}

	public void setZ(float z) {
		this.a31 = 0;
		this.a32 = 0;
		this.a33 = z;
	}

	@Override
	public Mat3f scale(float scaleX, float scaleY) {
		this.a11 *= scaleX;
		this.a21 *= scaleX;
		this.a31 *= scaleX;

		this.a12 *= scaleY;
		this.a22 *= scaleY;
		this.a32 *= scaleY;
		return this;
	}

	@Override
	public Mat scale(float x, float y, float z) {
		return this.scale(x, y);
	}

	public Mat3f offsetInverse(float x, float y) {
		this.a11 = this.a11 + x * this.a31;
		this.a12 = this.a12 + x * this.a32;
		this.a13 = this.a13 + x * this.a33;

		this.a21 = this.a21 + y * this.a31;
		this.a22 = this.a22 + y * this.a32;
		this.a23 = this.a23 + y * this.a33;
		return this;
	}

	public Mat3f scaleInverse(float scaleX, float scaleY) {
		this.a11 *= scaleX;
		this.a12 *= scaleX;
		this.a13 *= scaleX;

		this.a21 *= scaleY;
		this.a22 *= scaleY;
		this.a23 *= scaleY;
		return this;
	}

	@Override
	public Mat3f copy() {
		Mat3f mat = new Mat3f();
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

	public void pop(FloatStack stack) {
		this.a11 = stack.popFloat();
		this.a12 = stack.popFloat();
		this.a13 = stack.popFloat();
		this.a21 = stack.popFloat();
		this.a22 = stack.popFloat();
		this.a23 = stack.popFloat();
		this.a31 = stack.popFloat();
		this.a32 = stack.popFloat();
		this.a33 = stack.popFloat();
	}

	public void push(FloatStack stack) {
		stack.push(this.a33);
		stack.push(this.a32);
		stack.push(this.a31);
		stack.push(this.a23);
		stack.push(this.a22);
		stack.push(this.a21);
		stack.push(this.a13);
		stack.push(this.a12);
		stack.push(this.a11);
	}

	@Override
	public Mat3f load(Mat from) {
		if(from instanceof Mat3f m) {
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


	public Mat3f load(Mat3f mat) {
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

	@Override
	public String toString() {
		return String.format("[%03.3f, %03.3f, %03.3f]\n[%03.3f, %03.3f, %03.3f]\n[%03.3f, %03.3f, %03.3f]", this.a11,
			this.a12,
			this.a13,
			this.a21,
			this.a22,
			this.a23,
			this.a31,
			this.a32,
			this.a33
		);
	}
}
