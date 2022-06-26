package net.devtech.jerraria.util.math;

public interface Transformable {
	default Transformable offset(float x, float y) {
		return this.offset(x, y, 0);
	}

	Transformable offset(float x, float y, float z);

	default Transformable scale(float x, float y) {
		return this.scale(x, y, 1);
	}

	Transformable scale(float x, float y, float z);

	Transformable rotate(float radX, float radY, float radZ);

	default Transformable rotate(float rad) {
		return this.rotate(0, 0, rad);
	}
}
