package net.devtech.jerraria.util.math;

import it.unimi.dsi.fastutil.floats.FloatStack;
import net.devtech.jerraria.render.api.base.GlData;

public final class Mat2x3f extends Mat implements AutoCloseable {
	/**
	 * [a11 a12 a13]
	 * [a21 a22 a23]
	 * [  0   0   1]
	 */
	private float a11, a12, a13, a21, a22, a23;
	private float zOffset = 1;

	public Mat2x3f() {
		this.a22 = this.a11 = 1;
	}

	@Override
	public Mat2x3f identity() {
		this.a12 = this.a13 = this.a21 = this.a23 = 0;
		this.zOffset = this.a22 = this.a11 = 1;
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
		return Math.min(this.zOffset, z);
	}

	@Override
	public Mat2x3f offset(float x, float y, float z) {
		this.offset(x, y);
		this.setZ(z+this.zOffset);
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
	public Mat2x3f offset(float x, float y) {
		this.a13 = this.a11 * x + this.a12 * y + this.a13;
		this.a23 = this.a21 * x + this.a22 * y + this.a23;
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
		return this;
	}

	@Override
	public Mat rotate(float radX, float radY, float radZ) {
		return this.rotate(radZ);
	}

	public void setZ(float z) {
		this.zOffset = z;
	}

	@Override
	public Mat2x3f scale(float scaleX, float scaleY) {
		this.a11 *= scaleX;
		this.a21 *= scaleX;
		this.a12 *= scaleY;
		this.a22 *= scaleY;
		return this;
	}

	@Override
	public Mat scale(float x, float y, float z) {
		return this.scale(x, y);
	}

	@Override
	public Mat2x3f inverse(Mat mat) {
		Mat2x3f inverted = (Mat2x3f) mat;
		inverted.identity();

		float a11 = this.a11, a12 = this.a12, a13 = this.a13;
		float a21 = this.a21, a22 = this.a22, a23 = this.a23;

		float clean2w1 = a21 / a11;
		a21 -= a11 * clean2w1;
		a22 -= a12 * clean2w1;
		a23 -= a13 * clean2w1;
		inverted.a21 -= inverted.a11 * clean2w1;
		inverted.a22 -= inverted.a12 * clean2w1;
		inverted.a23 -= inverted.a13 * clean2w1;

		inverted.a23 -= a23;
		inverted.a13 -= a13;

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

		return inverted;
	}

	@Override
	public Mat2x3f copy() {
		Mat2x3f mat = new Mat2x3f();
		mat.a11 = this.a11;
		mat.a12 = this.a12;
		mat.a13 = this.a13;

		mat.a21 = this.a21;
		mat.a22 = this.a22;
		mat.a23 = this.a23;
		mat.zOffset = this.zOffset;
		return mat;
	}

	public void pop(FloatStack stack) {
		this.a11 = stack.popFloat();
		this.a12 = stack.popFloat();
		this.a13 = stack.popFloat();
		this.a21 = stack.popFloat();
		this.a22 = stack.popFloat();
		this.a23 = stack.popFloat();
	}

	public void push(FloatStack stack) {
		stack.push(this.a23);
		stack.push(this.a22);
		stack.push(this.a21);
		stack.push(this.a13);
		stack.push(this.a12);
		stack.push(this.a11);
	}

	@Override
	public Mat2x3f load(MatView from) {
		return this.load((Mat2x3f)from);
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

			default -> x == y ? 1 : 0;
		};
	}


	public Mat2x3f load(Mat2x3f mat) {
		this.a11 = mat.a11;
		this.a12 = mat.a12;
		this.a13 = mat.a13;
		this.a21 = mat.a21;
		this.a22 = mat.a22;
		this.a23 = mat.a23;
		this.zOffset = mat.zOffset;
		return this;
	}

	@Override
	public void upload3x3(GlData.Buf buf) {
		buf.f(this.a11).f(this.a21).f(0)
		   .f(this.a12).f(this.a22).f(0)
		   .f(this.a13).f(this.a23).f(1);
	}

	@Override
	public void upload4x4(GlData.Buf buf) {
		buf.f(this.a11).f(this.a21).f(0).f(0); // todo this is wrong I think
		buf.f(this.a12).f(this.a22).f(0).f(0);
		buf.f(this.a13).f(this.a23).f(1).f(0);
		buf.f(0)    .f(0)     .f(0).f(1);
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
			0,
			0,
			1
		);
	}

	@Override
	public MatType getType() {
		return MatType.MAT3;
	}

	public static final class Type extends MatType {
		@Override
		public Mat createIdentity() {
			return new Mat2x3f();
		}
	}
}
