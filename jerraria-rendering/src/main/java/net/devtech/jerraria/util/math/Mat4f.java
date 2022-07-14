package net.devtech.jerraria.util.math;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicReference;

import net.devtech.jerraria.render.api.base.GlData;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vector4f;

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
		return MatType.MAT4;
	}

	@Override
	public Mat copy() {
		return new Mat4f(this.mat.copy());
	}

	@Override
	public Mat inverse(Mat mat) {
		Matrix4f copy = this.mat.copy();
		copy.invert();
		return new Mat4f(copy);
	}

	public Vector4f transform(float x, float y, float z, float w) {
		Vector4f vector = new Vector4f(x, y, z, w);
		vector.transform(this.mat);
		return vector;
	}

	@Override
	public float mulX(float x, float y, float z, float w) {
		return this.transform(x, y, z, w).getX();
	}

	@Override
	public float mulY(float x, float y, float z, float w) {
		return this.transform(x, y, z, w).getY();
	}

	@Override
	public float mulZ(float x, float y, float z, float w) {
		return this.transform(x, y, z, w).getZ();
	}

	@Override
	public float mulW(float x, float y, float z, float w) {
		return this.transform(x, y, z, w).getW();
	}

	@Override
	public void upload(GlData.Buf buf, int m, int n) {
		if(buf instanceof GlData.FlushableBuf f) {
			FloatBuffer fb = f.fb();
			this.mat.writeColumnMajor(fb);
			f.flush0();
		} else {
			FloatBuffer temp = FloatBuffer.allocate(16);
			this.mat.writeColumnMajor(temp);
			for(int i = 0; i < temp.position(); i++) {
				buf.f(temp.get(i));
			}
		}
	}

	@Override
	public float getElement(int x, int y) { // extremely unperformant
		FloatBuffer buffer = FloatBuffer.allocate(16);
		this.mat.writeColumnMajor(buffer);
		return buffer.get(x*4+y);
	}

	public static class Type extends MatType {
		@Override
		public Mat createIdentity() {
			return new Mat4f();
		}
	}
}
