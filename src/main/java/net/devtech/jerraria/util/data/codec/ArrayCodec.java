package net.devtech.jerraria.util.data.codec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.function.IntFunction;

import net.devtech.jerraria.util.data.bin.BinCodec;

public class ArrayCodec<T> implements BinCodec<T> {
	public static final BinCodec<byte[]> BYTE = BinCodec.from((output, value) -> {
		output.writeInt(value.length);
		output.write(value);
	}, input -> {
		byte[] buf = new byte[input.readInt()];
		input.readFully(buf);
		return buf;
	});
	public static final BinCodec<int[]> INT = new ArrayCodec<>(int[]::new, (i, a, idx) -> a[idx] = i.readInt(), (o, a, idx) -> o.writeInt(a[idx]));
	public static final BinCodec<long[]> LONG = new ArrayCodec<>(long[]::new, (i, a, idx) -> a[idx] = i.readLong(), (o, a, idx) -> o.writeLong(a[idx]));

	final IntFunction<T> init;
	final Reader<T> reader;
	final Writer<T> writer;

	public ArrayCodec(IntFunction<T> init, Reader<T> reader, Writer<T> writer) {
		this.init = init;
		this.reader = reader;
		this.writer = writer;
	}

	@Override
	public T read(DataInput input) throws IOException {
		int value = input.readInt();
		T array = this.init.apply(value);
		for(int i = 0; i < value; i++) {
			this.reader.readValue(input, array, i);
		}
		return array;
	}

	@Override
	public void write(DataOutput output, T value) throws IOException {
		int length = Array.getLength(value);
		output.writeInt(length);
		for(int i = 0; i < length; i++) {
			this.writer.writeValue(output, value, i);
		}
	}

	public interface Reader<T> {
		void readValue(DataInput input, T array, int index) throws IOException;
	}

	public interface Writer<T> {
		void writeValue(DataOutput output, T array, int index) throws IOException;
	}
}
