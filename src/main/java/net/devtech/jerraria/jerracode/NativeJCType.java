package net.devtech.jerraria.jerracode;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLongImmutablePair;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Vec2d;
import net.devtech.jerraria.jerracode.bin.BinCodec;
import net.devtech.jerraria.jerracode.bin.BinDecode;
import net.devtech.jerraria.jerracode.bin.BinEncode;
import net.devtech.jerraria.jerracode.codec.ArrayCodec;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.jerracode.internal.PairJCType;
import net.devtech.jerraria.jerracode.pool.JCDecodePool;
import net.devtech.jerraria.world.entity.SerializedEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Natively supported Jercode types
 */
public final class NativeJCType<T> implements JCType<T, T> {
	static final NativeJCType<?>[] BY_ID = new NativeJCType[256];
	public static final NativeJCType<Integer> INT = new NativeJCType<>((p, i) -> i.readInt(),
		(p, o, v) -> o.writeInt(v));
	public static final NativeJCType<Long> LONG = new NativeJCType<>(
		(p, i) -> i.readLong(),
		(p, o, v) -> o.writeLong(v));
	public static final NativeJCType<IntList> INT_ARRAY = new NativeJCType<>(ArrayCodec.INT);
	public static final NativeJCType<ByteList> BYTE_ARRAY = new NativeJCType<>(ArrayCodec.BYTE);
	public static final NativeJCType<LongList> LONG_ARRAY = new NativeJCType<>(ArrayCodec.LONG);
	public static final NativeJCType<String> STRING = new NativeJCType<>((p, i) -> i.readUTF(),
		(p, o, v) -> o.writeUTF(v));
	public static final NativeJCType<String> POOLED_STRING = pooled(STRING);
	public static final NativeJCType<List<String>> STRING_LIST = listType(STRING);
	public static final NativeJCType<JCElement> ANY = new NativeJCType<>((p, i) -> JCIO.getElement(p,
		i,
		JCIO.readType(i)), (p, o, v) -> v.write(p, o));
	public static final NativeJCType<List<JCElement>> ANY_LIST_MIXED = listType(ANY);
	public static final NativeJCType<JCList<?>> ANY_LIST_SAME = new NativeJCType<>((pool, input) -> {
		int len = input.readInt();
		NativeJCType<?> id = JCIO.readType(input);
		return getList(pool, input, id, len);
	}, (pool, output, value) -> {
		output.writeInt(value.size());
		NativeJCType type = value.getNativeType();
		JCIO.writeType(output, type);
		for(Object element : value) {
			type.encode().write(pool, output, element);
		}
	});
	public static final NativeJCType<JCTagView> TAG = new NativeJCType<>((p, input) -> {
		JCTagView.Builder builder = JCTagView.builder();
		int len = input.readInt();
		for(int i = 0; i < len; i++) {
			JCIO.readEntry(input, p, builder);
		}
		return builder.build();
	}, (o, output, value) -> {
		output.writeInt(value.getKeys().size());
		value.forEach(new JCTagView.ValuesConsumer() {
			@Override
			public <L, N> void accept(String key, JCType<L, N> type, L value) throws IOException {
				var nat = type.convertToNative(value);
				POOLED_STRING.encode.write(o, output, key);
				JCIO.write(type.nativeType(), o, output, nat);
			}
		});
	});
	public static final NativeJCType<List<JCTagView>> TAG_LIST = listType(TAG);
	public static final NativeJCType<Id.Full> PACKED_ID = new NativeJCType<>((pool, input) -> {
		return Id.create(input.readLong(), input.readLong());
	}, (pool, output, value) -> {
		output.writeLong(value.getPackedNamespace());
		output.writeLong(value.getPath());
	});
	public static final NativeJCType<Id.Full> POOLED_PACKED_ID = pooled(PACKED_ID);
	public static final NativeJCType<JCTagView> POOLED_TAG = pooled(TAG);
	public static final NativeJCType<List<JCTagView>> POOLED_TAG_LIST = listType(POOLED_TAG);
	public static final NativeJCType<Pair<JCElement, JCElement>> PAIR = NativeJCType.pairType(
		ANY,
		ANY,
		ObjectObjectImmutablePair::new);
	public static final NativeJCType<Pair<Id.Full, JCElement>> ID_ANY = NativeJCType.pairType(
		PACKED_ID,
		ANY,
		ObjectObjectImmutablePair::new);
	public static final NativeJCType<IntObjectPair<JCElement>> INT_ANY = NativeJCType.pairType(
		INT,
		ANY,
		IntObjectImmutablePair::new);
	public static final NativeJCType<List<IntObjectPair<JCElement>>> INT_ANY_LIST = listType(INT_ANY);
	public static final NativeJCType<IntLongPair> INT_LONG = NativeJCType.pairType(
		INT,
		LONG,
		IntLongImmutablePair::new);
	public static final NativeJCType<List<Pair<Id.Full, JCElement>>> ID_ANY_LIST = listType(ID_ANY);
	public static final NativeJCType<List<IntLongPair>> INT_LONG_LIST = listType(INT_LONG);
	public static final NativeJCType<Vec2d> POS = new NativeJCType<>((pool, input) -> {
		return new Vec2d(input.readDouble(), input.readDouble());
	}, (pool, output, value) -> {
		output.writeDouble(value.x());
		output.writeDouble(value.y());
	});
	static final NativeJCType<SerializedEntity> ENTITY = pairType(POS, ANY, SerializedEntity::new).unregister();
	static final NativeJCType<List<SerializedEntity>> ENTITIES_OF_SAME_TYPE = listType(ENTITY).unregister();
	static final NativeJCType<Pair<Id.Full, List<SerializedEntity>>> ENTITY_GROUP = pairType(PACKED_ID, ENTITIES_OF_SAME_TYPE, ObjectObjectImmutablePair::new);
	public static final NativeJCType<List<Pair<Id.Full, List<SerializedEntity>>>> ENTITIES = listType(ENTITY_GROUP);
	static int idCounter;
	private final BinDecode<T> decode;
	private final BinEncode<T> encode;
	private final int id;
	private final Int2IntOpenHashMap pairTypes = new Int2IntOpenHashMap();

	/**
	 */
	public NativeJCType(BinDecode<T> decode, BinEncode<T> encode, int id) {
		this.decode = decode;
		this.encode = encode;
		this.id = id;
		this.pairTypes.defaultReturnValue(-1);
	}

	public static <T> NativeJCType<JCList<T>> listAny(NativeJCType<T> componentType) {
		return (NativeJCType) ANY_LIST_SAME;
	}

	NativeJCType(BinDecode<T> decode, BinEncode<T> encode) {
		this(decode, encode, idCounter++);
		BY_ID[this.id] = this;
	}

	NativeJCType(BinCodec<T> codec) {
		this(codec, codec);
	}

	NativeJCType<T> unregister() {
		idCounter--;
		BY_ID[this.id] = null;
		return this;
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

	@Override
	@SuppressWarnings("unchecked")
	public <C> JCType<Pair<T, C>, ?> pairType(JCType<C, ?> type) {
		int i = type instanceof NativeJCType n ? this.pairTypes.get(n.id) : -1;
		if(i == -1) {
			return new PairJCType<>(this, type);
		} else {
			return (JCType) BY_ID[i];
		}
	}

	@NotNull
	private static <T> JCList<T> getList(JCDecodePool pool, DataInput input, NativeJCType<T> id, int len)
		throws IOException {
		ImmutableList.Builder<T> elements = new ImmutableList.Builder<>();
		for(int i = 0; i < len; i++) {
			elements.add(id.decode().read(pool, input));
		}
		return new JCList<>(elements.build(), id);
	}

	@SuppressWarnings("unchecked")
	static <T> NativeJCType<T> pooled(NativeJCType<T> type) {
		return new NativeJCType<>((pool, input) -> {
			return (T) pool.getElement(input.readInt()).value();
		}, (pool, output, value) -> {
			int index = pool.getIndex(JCElement.create(type, value));
			output.writeInt(index);
		});
	}

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

	static <K, V> NativeJCType<Map<K, V>> mapType(NativeJCType<K> keyType, NativeJCType<V> valueType) {
		return new NativeJCType<>((pool, input) -> {
			int i = input.readInt();
			ImmutableMap.Builder<K, V> map = ImmutableMap.builderWithExpectedSize(i);
			for(int $ = 0; $ < i; $++) {
				map.put(keyType.decode.read(pool, input), valueType.decode.read(pool, input));
			}
			return map.build();
		}, (pool, output, value) -> {
			output.writeInt(value.size());
			for(var entry : value.entrySet()) {
				K k = entry.getKey();
				V v = entry.getValue();
				keyType.encode.write(pool, output, k);
				valueType.encode.write(pool, output, v);
			}
		});
	}

	static <A, B, T extends Pair<A, B>> NativeJCType<T> pairType(NativeJCType<A> a,
		NativeJCType<B> b,
		BiFunction<A, B, T> pairCreator) {
		NativeJCType<T> type = new NativeJCType<>((pool, input) -> {
			return pairCreator.apply(a.decode.read(pool, input), b.decode.read(pool, input));
		}, (pool, output, value) -> {
			a.encode.write(pool, output, value.first());
			b.encode.write(pool, output, value.second());
		});
		a.pairTypes.put(b.id, type.id);
		return type;
	}

	public BinDecode<T> decode() {return decode;}

	public BinEncode<T> encode() {return encode;}

	public int id() {return id;}

	@Override
	public String toString() {
		return "NativeJCType[id=" + id + "]";
	}
}
