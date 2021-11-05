package net.devtech.jerraria.util.data;

import java.io.DataOutput;
import java.io.IOException;

import net.devtech.jerraria.util.data.pool.JCEncodePool;

public record JCElement<T>(NativeJCType<T> type, T value) {
	public void write(JCEncodePool pool, DataOutput output) throws IOException {
		JCIO.write(this.type, pool, output, this.value);
	}
}
