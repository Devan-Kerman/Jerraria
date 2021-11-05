package net.devtech.jerraria.util.data.pool;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.util.data.JCElement;
import net.devtech.jerraria.util.data.JCIO;

public class JCDecodePool {
	final List<JCElement<?>> elements = new ArrayList<>();

	public JCElement<?> getElement(int poolId) {
		return this.elements.get(poolId);
	}

	public void read(DataInput input) throws IOException {
		int elements = input.readInt();
		for(int i = 0; i < elements; i++) {
			this.elements.add(JCIO.read((JCDecodePool)null, input));
		}
	}
}
