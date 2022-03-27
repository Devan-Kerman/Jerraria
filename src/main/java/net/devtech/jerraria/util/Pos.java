package net.devtech.jerraria.util;

public record Pos(double x, double y) implements Positioned {
	public Pos withX(double x) {
		return new Pos(x, this.y);
	}

	public Pos withY(double y) {
		return new Pos(this.x, y);
	}

	public Pos withOffset(double x, double y) {
		return new Pos(this.x + x, this.y + y);
	}

	public Pos withXOffset(double x) {
		return new Pos(this.x + x, this.y);
	}

	public Pos withYOffset(double y) {
		return new Pos(this.x, this.y + y);
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
