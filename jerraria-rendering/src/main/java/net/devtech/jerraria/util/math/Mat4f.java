package net.devtech.jerraria.util.math;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

public final class Mat4f extends Mat {
	final Matrix4f mat;

	public Mat4f(Matrix4f mat) {
		this.mat = mat;
	}

	public Mat4f() {
		this(new Matrix4f());
		this.mat.loadIdentity();
	}

	@Override
	public Mat offset(float x, float y, float z) {
		this.mat.multiplyByTranslation(x, y, z);
		return this;
	}

	@Override
	public Mat scale(float x, float y, float z) {
		this.mat.multiply(Matrix4f.scale(x, y, z));
		return this;
	}

	@Override
	public Mat rotate(float radX, float radY, float radZ) {
		Quaternion quaternion = new Quaternion(radX, radY, radZ, false);
		this.mat.multiply(quaternion);
		return this;
	}

	@Override
	public Mat load(MatView from) {
		this.mat.load(((Mat4f)from).mat);
		return this;
	}

	@Override
	public Mat identity() {
		this.mat.loadIdentity();
		return this;
	}

	@Override
	public MatType getType() {
		return this;
	}

	@Override
	public Mat copy() {
		return this;
	}

	@Override
	public Mat inverse(Mat mat) {
		return this;
	}

	@Override
	public float mulX(float x, float y, float z, float w) {
		return 0;
	}

	@Override
	public float mulY(float x, float y, float z, float w) {
		return 0;
	}

	@Override
	public float mulZ(float x, float y, float z, float w) {
		return 0;
	}

	@Override
	public float mulW(float x, float y, float z, float w) {
		return 0;
	}

	@Override
	public float getElement(int x, int y) {
		return 0;
	}
}
