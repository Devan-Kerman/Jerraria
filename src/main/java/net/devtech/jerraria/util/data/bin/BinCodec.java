package net.devtech.jerraria.util.data.bin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.devtech.jerraria.util.data.pool.JCDecodePool;
import net.devtech.jerraria.util.data.pool.JCEncodePool;

public interface BinCodec<T> extends BinDecode<T>, BinEncode<T> {
	static <T> BinCodec<T> from(BinEncode<T> encode, BinDecode<T> decode) {
		return new BinCodec<>() {
			@Override
			public T read(JCDecodePool pool, DataInput input) throws IOException {
				return decode.read(pool, input);
			}

			@Override
			public void write(JCEncodePool pool, DataOutput output, T value) throws IOException {
				encode.write(pool, output, value);
			}
		};
	}
}
