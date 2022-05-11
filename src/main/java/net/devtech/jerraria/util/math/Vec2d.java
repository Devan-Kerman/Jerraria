package net.devtech.jerraria.util.math;

public record Vec2d(double x, double y) implements Pos2d {
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

	public static final class Mut implements Pos2d {
		public double x, y;

		public Mut() {
		}

		public Mut(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public Mut set(double x, double y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Mut setX(double x) {
			this.x = x;
			return this;
		}

		public Mut setY(double y) {
			this.y = y;
			return this;
		}

		@Override
		public double x() {
			return this.x;
		}

		@Override
		public double y() {
			return this.y;
		}
	}
}
