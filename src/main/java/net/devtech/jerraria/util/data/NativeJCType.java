package net.devtech.jerraria.util.data;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.devtech.jerraria.util.data.bin.BinCodec;
import net.devtech.jerraria.util.data.bin.BinDecode;
import net.devtech.jerraria.util.data.bin.BinEncode;
import net.devtech.jerraria.util.data.codec.ArrayCodec;

/**
 * Natively supported Jercode types
 */
public record NativeJCType<T>(BinDecode<T> decode, BinEncode<T> encode, int id) implements JCType<T, T> {
	static final NativeJCType<?>[] BY_ID = new NativeJCType[256];

	public static final NativeJCType<Integer> INT = new NativeJCType<>(
		(p, i) -> i.readInt(),
		(p, o, v) -> o.writeInt(v));
	public static final NativeJCType<IntList> INT_ARRAY = new NativeJCType<>(ArrayCodec.INT);
	public static final NativeJCType<ByteList> BYTE_ARRAY = new NativeJCType<>(ArrayCodec.BYTE);
	public static final NativeJCType<LongList> LONG_ARRAY = new NativeJCType<>(ArrayCodec.LONG);
	public static final NativeJCType<String> STRING = new NativeJCType<>(
		(p, i) -> i.readUTF(),
		(p, o, v) -> o.writeUTF(v));
	public static final NativeJCType<List<String>> STRING_LIST = listType(STRING);
	public static final NativeJCType<JCElement<?>> ANY = new NativeJCType<>((p, i) -> JCIO.getElement(p, i, JCIO.readType(i)), (p, o, v) -> v.write(p, o));
	public static final NativeJCType<List<JCElement<?>>> ANY_LIST = listType(ANY);
	public static final NativeJCType<JCTagView> TAG = new NativeJCType<>((p, input) -> {
		JCTagView.Builder builder = JCTagView.builder();
		for(int i = 0; i < input.readInt(); i++) {
			var type = JCIO.readType(input);
			JCIO.readEntry(input, p, builder, type);
		}
		return builder.build();
	}, (o, output, value) -> {
		output.writeInt(value.getKeys().size());
		value.forEach(new JCTagView.ValuesConsumer() {
			@Override
			public <L, N> void accept(String key, JCType<L, N> type, L value) throws IOException {
				var nat = type.convertToNative(value);
				output.writeUTF(key);
				JCIO.write(type.nativeType(), o, output, nat);
			}
		});
	});


	static int idCounter;
	/*static <T> NativeJCType<T> pooled(NativeJCType<T> type) {
		return new NativeJCType<>((pool, input) -> {

		}, (pool, output, value) -> {

		});
	}*/

	static <T> NativeJCType<List<T>> listType(NativeJCType<T> type) {
		return new NativeJCType<>((pool, input) -> {
			int size = input.readInt();
			ImmutableList.Builder<T> list = ImmutableList.builderWithExpectedSize(size);
			for(int i = 0; i < size; i++) {
				list.add(type.decode.read(pool, input));
			}
			return list.build();
		}, (pool, output, value) -> {
			output.writeInt(value.size());
			for(T t : value) {
				type.encode.write(pool, output, t);
			}
		});
	}

	public NativeJCType(BinDecode<T> decode, BinEncode<T> encode) {
		this(decode, encode, idCounter++);
		BY_ID[this.id] = this;
	}

	public NativeJCType(BinCodec<T> codec) {
		this(codec, codec);
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
