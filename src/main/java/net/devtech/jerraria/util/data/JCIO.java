package net.devtech.jerraria.util.data;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import net.devtech.jerraria.util.data.pool.JCDecodePool;
import net.devtech.jerraria.util.data.pool.JCEncodePool;
import org.jetbrains.annotations.NotNull;

public class JCIO {
	public static <T> void write(NativeJCType<T> type, T value, DataOutput output) throws IOException {
		JCEncodePool pool = new JCEncodePool();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		type.encode().write(pool, dos, value);
		pool.write(output);
		output.write(baos.toByteArray());
	}

	public static <T> T read(NativeJCType<T> type, DataInput input) throws IOException {
		JCDecodePool pool = new JCDecodePool();
		pool.read(input);
		return type.decode().read(pool, input);
	}

	public static <T> void write(NativeJCType<T> type, JCEncodePool pool, DataOutput output, T value) throws IOException {
		output.writeByte(type.id());
		type.encode().write(pool, output, value);
	}

	public static NativeJCType<?> readType(DataInput input) throws IOException {
		int id = input.readUnsignedByte();
		return NativeJCType.BY_ID[id];
	}

	public static <T> T read(NativeJCType<T> type, JCDecodePool pool, DataInput input) throws IOException {
		return type.decode().read(pool, input);
	}

	public static void writeType(DataOutput output, NativeJCType<?> type) throws IOException {
		output.writeByte(type.id());
	}

	@NotNull
	static <T> JCElement<T> getElement(JCDecodePool pool, DataInput i, NativeJCType<T> type) throws IOException {
		return new JCElement<>(type, read(type, pool, i));
	}

	public static <T> JCElement<T> read(JCDecodePool pool, DataInput input) throws IOException {
		return (JCElement<T>) getElement(pool, input, readType(input));
	}

	static <T> void readEntry(DataInput input, JCDecodePool pool, JCTagView.Builder builder, NativeJCType<T> type)
		throws IOException {
		var key = NativeJCType.POOLED_STRING.decode().read(pool, input);
		builder.put(key, type, read(type, pool, input));
	}
}
