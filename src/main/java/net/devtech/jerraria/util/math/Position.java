package net.devtech.jerraria.util.math;

public record Position(double x, double y) implements Positioned {
	public Position withX(double x) {
		return new Position(x, this.y);
	}

	public Position withY(double y) {
		return new Position(this.x, y);
	}

	public Position withOffset(double x, double y) {
		return new Position(this.x + x, this.y + y);
	}

	public Position withXOffset(double x) {
		return new Position(this.x + x, this.y);
	}

	public Position withYOffset(double y) {
		return new Position(this.x, this.y + y);
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
