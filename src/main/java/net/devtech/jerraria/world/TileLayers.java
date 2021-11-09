package net.devtech.jerraria.world;

import java.util.List;

import net.devtech.jerraria.util.Log2;

public enum TileLayers {
	BLOCK,
	WALL,
	FLUID,
	WIRE;

	public static final List<TileLayers> LAYERS = List.of(values());
	public static final int COUNT = LAYERS.size();
	public static final int COUNT_LOG2 = Log2.log2(COUNT);
	public static final int COUNT_MASK = Log2.nearestPowerOf2(COUNT) - 1;
}
