package net.devtech.jerraria.util.math;

public interface SimpleShape {
	/**
	 * @return true if the shape is entirely enclosed by the given bounding box
	 */
	boolean isEnclosed(double fromX, double fromY, double toX, double toY);

	/**
	 * @return true if the shape contacts the given bounding box
	 */
	boolean doesIntersect(double fromX, double fromY, double toX, double toY);

	Rectangle maxBounds();

	record Rectangle(double width, double height) implements SimpleShape {
		@Override
		public boolean isEnclosed(double fromX, double fromY, double toX, double toY) {
			return fromX <= 0 && fromY <= 0 && toX >= this.width && toY >= this.height;
		}

		@Override
		public boolean doesIntersect(double fromX, double fromY, double toX, double toY) {
			double width = this.width, height = this.height;
			return fromX <= width && fromY <= height && toX >= width && toY >= height;
		}

		@Override
		public Rectangle maxBounds() {
			return this;
		}

	}
}
