package net.devtech.jerraria.util.math;

import net.devtech.jerraria.render.api.base.GlData;

public abstract sealed class Mat permits Matrix3f {
	Mat() {}

	public float mulX(float x, float y) {
		return this.mulX(x, y, 1, 1);
	}

	public float mulY(float x, float y) {
		return this.mulY(x, y, 1, 1);
	}

	public float mulX(float x, float y, float z) {
		return this.mulX(x, y, z, 1);
	}

	public float mulY(float x, float y, float z) {
		return this.mulY(x, y, z, 1);
	}

	public float mulZ(float x, float y, float z) {
		return this.mulZ(x, y, z, 1);
	}

	public Mat offset(float x, float y) {
		return this.offset(x, y, 0);
	}

	public abstract Mat offset(float x, float y, float z);

	public Mat scale(float x, float y) {
		return this.scale(x, y, 1);
	}

	public abstract Mat scale(float x, float y, float z);

	public abstract Mat copy();

	public abstract Mat load(Mat from);

	public abstract float mulX(float x, float y, float z, float w);

	public abstract float mulY(float x, float y, float z, float w);

	public abstract float mulZ(float x, float y, float z, float w);

	public abstract float mulW(float x, float y, float z, float w);

	public abstract float getElement(int x, int y);

	public void upload3x3(GlData.Buf buf) {
		this.upload(buf, 3, 3);
	}

	public void upload4x4(GlData.Buf buf) {
		this.upload(buf, 4, 4);
	}

	public void upload(GlData.Buf buf, int m, int n) {
		for(int y = 0; y < m; y++) {
			for(int x = 0; x < n; x++) {
				buf.f(this.getElement(x, y));
			}
		}
	}
}
