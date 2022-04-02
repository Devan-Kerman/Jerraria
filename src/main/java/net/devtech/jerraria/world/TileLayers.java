package net.devtech.jerraria.world;

import java.util.List;

import net.devtech.jerraria.util.math.JMath;

public enum TileLayers {
	BLOCK,
	WALL,
	FLUID,
	WIRE;

	public static final List<TileLayers> LAYERS = List.of(values());
	public static final int COUNT = LAYERS.size();
	public static final int COUNT_LOG2 = JMath.log2(COUNT);
	public static final int COUNT_MASK = JMath.nearestPowerOf2(COUNT) - 1;
}
