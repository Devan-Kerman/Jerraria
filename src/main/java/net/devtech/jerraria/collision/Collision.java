package net.devtech.jerraria.collision;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.entity.BaseEntity;
import net.devtech.jerraria.util.Polygon;

public class Collision {
	final List<Polygon> polygons = new ArrayList<>();

	public interface Behavior {
		/**
		 * @return the remaining 'fraction of a tick' to tick the entity's movement
		 */
		float collide(BaseEntity entity);
	}

	static class Solid implements Behavior {
		@Override
		public float collide(BaseEntity entity) {

			return 0;
		}
	}
}
