package net.devtech.jerraria.util.data.pool;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.util.data.JCElement;

public class JCEncodePool {
	int counter;
	final Object2IntMap<JCElement<?>> elementPool = new Object2IntOpenHashMap<>();

	public int getIndex(JCElement<?> element) {
		return this.elementPool.computeIfAbsent(element, i -> this.counter++);
	}
}
