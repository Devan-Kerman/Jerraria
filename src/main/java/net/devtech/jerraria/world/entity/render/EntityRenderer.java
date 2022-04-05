package net.devtech.jerraria.world.entity.render;

import net.devtech.jerraria.util.math.Matrix3f;

public interface EntityRenderer {
	EntityRenderer UNRENDERED = new EntityRenderer() {
		@Override
		public boolean canCull(double minX, double minY, double maxX, double maxY) {
			return true;
		}

		@Override
		public void renderEntity(Matrix3f matrix) {
		}
	};

	/**
	 * @param minX world x coordinate of the left of the screen
	 * @param minY world y coordinate of the top of the screen
	 * @param maxX world x coordinate of the right of the screen
	 * @param maxY world y coordinate of the bottom of the screen
	 * @return how to cull the entity
	 */
	boolean canCull(double minX, double minY, double maxX, double maxY);

	void renderEntity(Matrix3f matrix);
}
