package net.devtech.jerraria.util.data.bin;

import java.io.DataOutput;
import java.io.IOException;

public interface BinEncode<T> {
	void write(DataOutput output, T value) throws IOException;
}
