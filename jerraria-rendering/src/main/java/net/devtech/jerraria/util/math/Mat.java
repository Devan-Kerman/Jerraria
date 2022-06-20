package net.devtech.jerraria.util.math;

public abstract sealed class Mat extends MatView implements Transformable permits Matrix3f {
	Mat() {}

	@Override
	public Mat offset(float x, float y) {
		return this.offset(x, y, 0);
	}

	@Override
	public abstract Mat offset(float x, float y, float z);

	@Override
	public Mat scale(float x, float y) {
		return this.scale(x, y, 1);
	}

	@Override
	public abstract Mat scale(float x, float y, float z);

	public abstract Mat load(Mat from);
}
