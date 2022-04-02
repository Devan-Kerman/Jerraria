package net.devtech.jerraria.jerracode.bin;

import java.io.DataInput;
import java.io.IOException;

import net.devtech.jerraria.jerracode.pool.JCDecodePool;

public interface BinDecode<T> {
	T read(JCDecodePool pool, DataInput input) throws IOException;
}
