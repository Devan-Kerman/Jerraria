package net.devtech.jerraria.tile.func;

import net.devtech.jerraria.access.func.ArrayFunc;
import net.devtech.jerraria.tile.TileVariant;

public interface TileProperty<T> {
	static <T> ArrayFunc<TileProperty<T>> skipIf(T skip, T defaultValue) {
		return arr -> variant -> {
			for(TileProperty<T> property : arr) {
				T value = property.getProperty(variant);
				if(value != skip) {
					return value;
				}
			}
			return defaultValue;
		};
	}

	T getProperty(TileVariant variant);
}
