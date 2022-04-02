package net.devtech.jerraria.jerracode.codec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import net.devtech.jerraria.jerracode.bin.BinCodec;
import net.devtech.jerraria.jerracode.pool.JCDecodePool;
import net.devtech.jerraria.jerracode.pool.JCEncodePool;

public class ArrayCodec<T> implements BinCodec<T> {
	public static final BinCodec<ByteList> BYTE = BinCodec.from((t, output, value) -> {
		output.writeInt(value.size());
		if(value instanceof ByteArrayList l) {
			output.write(l.elements(), 0, l.size());
		} else {
			output.write(value.toByteArray());
		}
	}, (t, input) -> {
		ByteArrayList list = new ByteArrayList(input.readInt());
		input.readFully(list.elements());
		return ByteLists.unmodifiable(list);
	});
	public static final BinCodec<IntList> INT = arr(
		IntArrayList::new,
		(i, a, idx) -> a.add(i.readInt()),
		(o, a, idx) -> o.writeInt(a.getInt(idx)),
		IntLists::unmodifiable);

	public static final BinCodec<LongList> LONG = arr(
		LongArrayList::new,
		(i, a, idx) -> a.add(i.readLong()),
		(o, a, idx) -> o.writeLong(a.getLong(idx)),
		LongLists::unmodifiable);

	final IntFunction<T> init;
	final Reader<T> reader;
	final Writer<T> writer;
	final UnaryOperator<T> makeImmutable;

	public ArrayCodec(IntFunction<T> init, Reader<T> reader, Writer<T> writer, UnaryOperator<T> immutable) {
		this.init = init;
		this.reader = reader;
		this.writer = writer;
		this.makeImmutable = immutable;
	}

	@Override
	public T read(JCDecodePool decode, DataInput input) throws IOException {
		int value = input.readInt();
		T array = this.init.apply(value);
		for(int i = 0; i < value; i++) {
			this.reader.readValue(input, array, i);
		}
		return array;
	}

	@Override
	public void write(JCEncodePool encode, DataOutput output, T value) throws IOException {
		int length = Array.getLength(value);
		output.writeInt(length);
		for(int i = 0; i < length; i++) {
			this.writer.writeValue(output, value, i);
		}
	}

	static <T> BinCodec<T> arr(IntFunction<T> init, Reader<T> reader, Writer<T> writer, UnaryOperator<T> immutable) {
		return new ArrayCodec<>(init, reader, writer, immutable);
	}

	public interface Reader<T> {
		void readValue(DataInput input, T array, int index) throws IOException;
	}

	public interface Writer<T> {
		void writeValue(DataOutput output, T array, int index) throws IOException;
	}
}
