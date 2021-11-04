package net.devtech.jerraria.world;

import java.util.List;

public enum TileLayers {
	BLOCK,
	WALL,
	FLUID,
	WIRE;

	public static final List<TileLayers> LAYERS = List.of(values());
	public static final int COUNT = LAYERS.size();
}
