package net.devtech.jerraria.util.data.bin;

import java.io.DataInput;
import java.io.IOException;

public interface BinDecode<T> {
	T read(DataInput input) throws IOException;
}
