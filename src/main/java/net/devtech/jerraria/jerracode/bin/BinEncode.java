package net.devtech.jerraria.jerracode.bin;

import java.io.DataOutput;
import java.io.IOException;

import net.devtech.jerraria.jerracode.pool.JCEncodePool;

public interface BinEncode<T> {
	void write(JCEncodePool pool, DataOutput output, T value) throws IOException;
}
