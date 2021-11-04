package net.devtech.jerraria.world;

import net.devtech.jerraria.entity.Entity;

public interface EntityLayer {
	Iterable<Entity> getEntities(int fromX, int fromY, int toX, int toY);
}
