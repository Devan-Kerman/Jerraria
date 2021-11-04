package net.devtech.jerraria.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.devtech.jerraria.data.codec.ArrayCodec;
import net.devtech.jerraria.data.bin.BinCodec;
import net.devtech.jerraria.data.bin.BinDecode;
import net.devtech.jerraria.data.bin.BinEncode;

/**
 * Natively supported Jercode types
 */
public record NativeJCType<T>(BinDecode<T> decode, BinEncode<T> encode, int id) implements JCType<T, T> {
	public static final NativeJCType<Integer> INT = new NativeJCType<>(DataInput::readInt, DataOutput::writeInt);
	public static final NativeJCType<int[]> INT_ARRAY = new NativeJCType<>(ArrayCodec.INT);
	public static final NativeJCType<byte[]> BYTE_ARRAY = new NativeJCType<>(ArrayCodec.BYTE);
	public static final NativeJCType<String> STRING = new NativeJCType<>(DataInput::readUTF, DataOutput::writeUTF);

	public static final NativeJCType<JCTagView> TAG = new NativeJCType<>(input -> {
		JCTagView.Builder builder = JCTagView.builder();
		for(int i = 0; i < input.readInt(); i++) {
			var type = NativeJCType.readType(input);
			readEntry(input, builder, type);
		}
		return builder.build();
	}, (output, value) -> {
		output.writeInt(value.getKeys().size());
		value.forEach(new JCTagView.ValuesConsumer() {
			@Override
			public <L, N> void accept(String key, JCType<L, N> type, L value) throws IOException {
				var nat = type.convertToNative(value);
				output.writeUTF(key);
				write(type.nativeType(), output, nat);
			}
		});
	});

	private static <T> void readEntry(DataInput input, JCTagView.Builder builder, NativeJCType<T> type) throws IOException {
		builder.put(input.readUTF(), type, NativeJCType.read(type, input));
	}

	static final NativeJCType<?>[] BY_ID = new NativeJCType[256];
	static int idCounter;
	public NativeJCType(BinDecode<T> decode, BinEncode<T> encode) {
		this(decode, encode, idCounter++);
		BY_ID[this.id] = this;
	}

	public NativeJCType(BinCodec<T> codec) {
		this(codec, codec);
	}

	public static <T> void write(NativeJCType<T> type, DataOutput output, T value) throws IOException {
		output.writeByte(type.id);
		type.encode.write(output, value);
	}

	public static NativeJCType<?> readType(DataInput input) throws IOException {
		int id = input.readUnsignedByte();
		return BY_ID[id];
	}

	public static <T> T read(NativeJCType<T> type, DataInput input) throws IOException {
		return type.decode.read(input);
	}

	@Override
	public NativeJCType<T> nativeType() {
		return this;
	}

	@Override
	public T convertToNative(T value) {
		return value;
	}

	@Override
	public T convertFromNative(T value) {
		return value;
	}
}
