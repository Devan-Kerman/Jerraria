package net.devtech.jerraria.util.data.pool;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.util.data.JCElement;

public class JCDecodePool {
	final Int2ObjectMap<JCElement<?>> element = new Int2ObjectOpenHashMap<>();

	public JCElement<?> getElement(int poolId) {
		return this.element.get(poolId);
	}
}
