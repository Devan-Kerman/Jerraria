package net.devtech.jerraria.util.math;

public abstract sealed class Mat extends MatView implements Transformable permits Mat2x3f, Mat4f {
	public static Mat create() {
		return new Mat4f();
	}

	Mat() {}

	@Override
	public Mat offset(float x, float y) {
		return this.offset(x, y, 0);
	}

	@Override
	public Mat rotate(float rad) {
		return this.rotate(0, 0, rad);
	}

	@Override
	public Mat scale(float x, float y) {
		return this.scale(x, y, 1);
	}

	@Override
	public abstract Mat offset(float x, float y, float z);

	@Override
	public abstract Mat scale(float x, float y, float z);

	@Override
	public abstract Mat rotate(float radX, float radY, float radZ);

	public abstract Mat load(MatView from);

	public abstract Mat identity();
}
