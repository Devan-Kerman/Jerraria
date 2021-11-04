package net.devtech.jerraria.world;

/**
 * A world with restricted access for thread-safety
 */
public interface LocalWorld extends World {

	World getAbsoluteWorld();
}
