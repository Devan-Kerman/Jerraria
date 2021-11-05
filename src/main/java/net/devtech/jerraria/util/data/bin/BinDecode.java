package net.devtech.jerraria.util.data.bin;

import java.io.DataInput;
import java.io.IOException;

import net.devtech.jerraria.util.data.pool.JCDecodePool;
import net.devtech.jerraria.util.data.pool.JCEncodePool;

public interface BinDecode<T> {
	T read(JCDecodePool pool, DataInput input) throws IOException;
}
