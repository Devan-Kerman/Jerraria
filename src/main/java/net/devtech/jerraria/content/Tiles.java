package net.devtech.jerraria.content;

import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.world.tile.Tile;

public interface Tiles {
	AirTile AIR = new AirTile();

	Registry<Tile> REGISTRY = new Registry.Fast<>(AIR);
}
