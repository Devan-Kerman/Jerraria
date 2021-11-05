package net.devtech.jerraria.util.data.bin;

import java.io.DataOutput;
import java.io.IOException;

import net.devtech.jerraria.util.data.pool.JCEncodePool;

public interface BinEncode<T> {
	void write(JCEncodePool pool, DataOutput output, T value) throws IOException;
}
