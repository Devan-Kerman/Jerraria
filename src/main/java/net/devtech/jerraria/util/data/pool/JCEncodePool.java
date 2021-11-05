package net.devtech.jerraria.util.data.pool;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.util.data.JCElement;

public class JCEncodePool {
	int counter;
	final Object2IntMap<JCElement<?>> elementPool = new Object2IntOpenHashMap<>();

	public int getIndex(JCElement<?> element) {
		return this.elementPool.computeIfAbsent(element, i -> this.counter++);
	}

	public void write(DataOutput dos) throws IOException {
		JCElement<?>[] elements = new JCElement[this.counter];
		this.elementPool.forEach((element, i) -> elements[i] = element);
		dos.writeInt(elements.length);
		for(JCElement<?> element : elements) {
			element.write(null, dos);
		}
	}
}
