package net.devtech.jerraria.util.math;

public record Rectangle(double offX, double offY, double width, double height) {
	public Rectangle {
		if(width < 0) {
			throw new IllegalArgumentException("width < 0");
		}
		if(height < 0) {
			throw new IllegalArgumentException("height < 0");
		}
	}

	public Rectangle(double width, double height) {
		this(0, 0, width, height);
	}

	/**
	 * @return true if the shape is entirely enclosed by the given bounding box
	 */
	public boolean isEnclosed(double fromX, double fromY, double toX, double toY) {
		return offX >= fromX && offY >= fromY && (offX + width) <= toX && (offY + height) <= toY;
	}

	/**
	 * @return true if the shape contacts the given bounding box
	 */
	public boolean doesIntersect(double fromX, double fromY, double toX, double toY) {
		double l1x = this.offX;
		if(l1x >= toX || fromX >= (l1x + this.width)) {
			return false;
		}

		double l1y = this.offY;
		if((l1y + this.height) >= fromY || toY >= l1y) {
			return false;
		}

		return true;
	}

	public double endX() {
		return this.offX + width;
	}

	public double endY() {
		return this.offY + height;
	}
}
