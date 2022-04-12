package net.devtech.jerraria.util.math;

public record Vec2d(double x, double y) implements Positioned {
	public Vec2d withX(double x) {
		return new Vec2d(x, this.y);
	}

	public Vec2d withY(double y) {
		return new Vec2d(this.x, y);
	}

	public Vec2d withOffset(double x, double y) {
		return new Vec2d(this.x + x, this.y + y);
	}

	public Vec2d withXOffset(double x) {
		return new Vec2d(this.x + x, this.y);
	}

	public Vec2d withYOffset(double y) {
		return new Vec2d(this.x, this.y + y);
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}
}
