package net.devtech.jerraria.util.data.pool;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.util.data.JCIO;
import net.devtech.jerraria.util.data.element.JCElement;

public class JCEncodePool {
	final Object2IntMap<JCElement<?>> elementPool = new Object2IntOpenHashMap<>();
	int counter;

	public int getIndex(JCElement<?> element) {
		return this.elementPool.computeIfAbsent(element, i -> this.counter++);
	}

	public void write(DataOutput output) throws IOException {
		int current = 0;
		List<ByteArrayOutputStream> all = new ArrayList<>();

		do {
			JCElement<?>[] elements = new JCElement[this.counter - current];
			int temp = current;
			this.elementPool.forEach((element, i) -> {
				if(i >= temp) {
					elements[i - temp] = element;
				}
			});

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for(JCElement<?> element : elements) {
				dos.writeInt(current);
				element.write(this, dos);
				current++;
			}
			all.add(baos);
		} while(current < this.counter);

		output.writeInt(current);
		for(int i = all.size() - 1; i >= 0; i--) {
			output.write(all.get(i).toByteArray());
		}
	}


}
