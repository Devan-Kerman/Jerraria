package net.devtech.jerraria.collision;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.entity.Entity;
import net.devtech.jerraria.util.Polygon;

public class Collision {
	final List<Polygon> polygons = new ArrayList<>();



	public interface Behavior {
		/**
		 * @return the remaining 'fraction of a tick' to tick the entity's movement
		 */
		float collide(Entity entity);
	}

	static class Solid implements Behavior {
		@Override
		public float collide(Entity entity) {
			double dx = entity.getDx(), dy = entity.getDy();


			return 0;
		}
	}
}
