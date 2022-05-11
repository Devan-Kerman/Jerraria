package net.devtech.jerraria.util.math;

public record Vec2i(int x, int y) implements Pos2i {
	public Vec2i withX(int x) {
		return new Vec2i(x, this.y);
	}

	public Vec2i withY(int y) {
		return new Vec2i(this.x, y);
	}

	public Vec2i withOffset(int x, int y) {
		return new Vec2i(this.x + x, this.y + y);
	}

	public Vec2i withXOffset(int x) {
		return new Vec2i(this.x + x, this.y);
	}

	public Vec2i withYOffset(int y) {
		return new Vec2i(this.x, this.y + y);
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	public static class Mut implements Pos2i {
		public int x, y;

		public Mut() {
		}

		public Mut(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Vec2i.Mut set(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Vec2i.Mut setX(int x) {
			this.x = x;
			return this;
		}

		public Vec2i.Mut setY(int y) {
			this.y = y;
			return this;
		}

		@Override
		public int getX() {
			return this.x;
		}

		@Override
		public int getY() {
			return this.y;
		}
	}
}
