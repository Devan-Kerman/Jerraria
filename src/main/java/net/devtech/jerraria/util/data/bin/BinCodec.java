package net.devtech.jerraria.util.data.bin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface BinCodec<T> extends BinDecode<T>, BinEncode<T> {
	static <T> BinCodec<T> from(BinEncode<T> encode, BinDecode<T> decode) {
		return new BinCodec<>() {
			@Override
			public T read(DataInput input) throws IOException {
				return decode.read(input);
			}

			@Override
			public void write(DataOutput output, T value) throws IOException {
				encode.write(output, value);
			}
		};
	}
}
