package net.devtech.jerraria.world;

import java.util.stream.Stream;

import net.devtech.jerraria.entity.Entity;

public interface EntityLayer {

	/**
	 * @return all the entities completely enclosed in the given area
	 */
	Stream<Entity> getEntities(EntitySearchType type, int fromX, int fromY, int toX, int toY);

	@SuppressWarnings("unchecked")
	default <T> Stream<T> getEntities(Class<T> class_, EntitySearchType type, int fromX, int fromY, int toX, int toY) {
		return (Stream<T>) getEntities(type, fromX, fromY, toX, toY)
			       .filter(class_::isInstance);
	}
}
