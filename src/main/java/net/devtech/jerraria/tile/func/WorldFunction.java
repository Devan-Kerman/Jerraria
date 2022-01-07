package net.devtech.jerraria.tile.func;

import net.devtech.jerraria.world.World;
import net.devtech.jerraria.tile.VariantConvertable;

public interface WorldFunction<T> {
	T get(World world, VariantConvertable convertable, int x, int y);

	default boolean requiresBlockData() {
		return true;
	}
}
