package net.devtech.jerraria.util.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.devtech.jerraria.util.data.pool.JCDecodePool;
import net.devtech.jerraria.util.data.pool.JCEncodePool;
import org.jetbrains.annotations.NotNull;

public class JCIO {

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

	@NotNull
	static <T> JCElement<T> getElement(JCDecodePool pool, DataInput i, NativeJCType<T> type) throws IOException {
		return new JCElement<>(type, read(type, pool, i));
	}

	static <T> void readEntry(DataInput input, JCDecodePool pool, JCTagView.Builder builder, NativeJCType<T> type)
		throws IOException {
		builder.put(input.readUTF(), type, read(type, pool, input));
	}
}
