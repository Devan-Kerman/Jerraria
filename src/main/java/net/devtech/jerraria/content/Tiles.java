package net.devtech.jerraria.content;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.tile.Tile;

public interface Tiles {
	AirTile AIR = new AirTile();
	Registry<Tile> REGISTRY = new Registry.Fast<>(AIR);

	Tile TEST = register(new Tile(), "test");

	Void __CLINIT__ = Validate.create(() -> {
		register(AIR, "air");
		return null;
	});

	private static <T extends Tile> T register(T tile, String name) {
		return REGISTRY.register(Id.createFull("jerraria", name), tile);
	}
}
