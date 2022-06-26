package net.devtech.jerraria.world.entity.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.devtech.jerraria.access.Access;
import net.devtech.jerraria.util.math.Mat3f;
import net.devtech.jerraria.world.entity.Entity;

public interface EntityRenderer {
	EntityRenderer UNRENDERED = (entity, matrix, windowMinX, windowMinY, windowMaxX, windowMaxY) -> {};

	/**
	 * A registry for adding additional
	 */
	Access<Function<Entity, EntityRenderer>> EXTRA_ENTITY_RENDERER = Access.create(array -> entity -> {
		CombinedEntityRenderer renderer = null;
		for(Function<Entity, EntityRenderer> function : array) {
			EntityRenderer apply = function.apply(entity);
			if(apply != null) {
				if(renderer == null) {
					renderer = new CombinedEntityRenderer();
				}
				renderer.renderers.add(apply);
			}
		}
		return renderer;
	});

	void renderEntity(Entity entity, Mat3f matrix, int windowFromX, int windowFromY, int windowToX, int windowToY);

	class CombinedEntityRenderer implements EntityRenderer {
		final List<EntityRenderer> renderers = new ArrayList<>();

		@Override
		public void renderEntity(Entity entity, Mat3f matrix, int windowFromX, int windowFromY, int windowToX, int windowToY) {
			for(EntityRenderer renderer : this.renderers) {
				renderer.renderEntity(entity, matrix, windowFromX, windowFromY, windowToX, windowToY);
			}
		}
	}
}
