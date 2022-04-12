package net.devtech.jerraria.world;

import java.util.stream.Stream;

import net.devtech.jerraria.world.entity.Entity;

public interface EntityLayer {

	/**
	 * @return all the entities completely enclosed in the given area
	 */
	Stream<Entity> getEntitiesEnclosed(EntitySearchType type, int fromX, int fromY, int toX, int toY);

	/**
	 * @param range how far out to search for entities that intersect, for example a large entity that spans multiple chunks
	 *  may be stored too far out of range to notice, if this is acceptable, choose a low range value
	 */
	default Stream<Entity> getEntitiesIntersect(EntitySearchType type, int fromX, int fromY, int toX, int toY, int range) {
		return this.getEntitiesEnclosed(type, fromX - range, fromY - range, toX + range, toY + range)
		           .filter(entity -> entity.doesIntersect(type, fromX, fromY, toX, toY));
	}

	@SuppressWarnings("unchecked")
	default <T> Stream<T> getEntities(Class<T> class_, EntitySearchType type, int fromX, int fromY, int toX, int toY) {
		return (Stream<T>) this.getEntitiesEnclosed(type, fromX, fromY, toX, toY)
		                       .filter(class_::isInstance);
	}
}
