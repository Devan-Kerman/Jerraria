package net.devtech.jerraria.world;

import net.devtech.jerraria.world.entity.Entity;

public interface EntityLayer {
	Iterable<Entity> getEntities(int fromX, int fromY, int toX, int toY);
}
