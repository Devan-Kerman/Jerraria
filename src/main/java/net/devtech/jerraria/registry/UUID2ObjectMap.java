package net.devtech.jerraria.registry;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.HashCommon;

public final class UUID2ObjectMap<T> extends AbstractMap<Id.Full, T> {
	private final float loadFactor;
	private long[] key1, key2;
	private Object[] value1;
	private int mask, size, maxFill;
	EntrySet entries;

	public static void main(String[] args) {
		UUID2ObjectMap<String> test = new UUID2ObjectMap<>(128);
		test.put(10, 10, "deez");
		System.out.println(test.get(10, 10));
	}

	public UUID2ObjectMap(int size) {
		this(.5f, size);
	}

	public UUID2ObjectMap(float factor, int size) {
		this(HashCommon.arraySize(size, factor), factor);
	}

	private UUID2ObjectMap(int n, float factor) {
		this.loadFactor = factor;
		this.resize(n);
	}

	@Override
	public EntrySet entrySet() {
		EntrySet set = this.entries;
		if(set == null) {
			this.entries = set = new EntrySet();
		}
		return set;
	}

	static int hash(long a, long b) {
		int hashA = (int)(a ^ (a >>> 32));
		int hashB = (int)(b ^ (b >>> 32));
		return 32 * (31 + hashA) + hashB;
	}

	public T put(long k1, long k2, T value) {
		final int pos = this.find(k1, k2);
		if(pos < 0) {
			int insertPos = -pos - 1;
			this.key1[insertPos] = k1;
			this.key2[insertPos] = k2;
			this.value1[insertPos] = value;

			if(this.size++ >= this.maxFill) {
				int size = arraySize(this.size + 1, this.loadFactor);
				long[] key1 = this.key1, key2 = this.key2;
				Object[] value1 = this.value1;
				this.resize(size);
				final int mask1 = size - 1; // Note that this is used by the hashing macro
				int pos1;
				for(int i = key2.length - 1; i >= 0; i--) {
					if(this.hasEntry(pos1 = HashCommon.mix(hash(key1[i], key2[i])) & mask1)) {
						while(this.hasEntry(pos1 = pos1 + 1 & mask1)) {
						}
					}

					this.key1[pos1] = key1[i];
					this.key2[pos1] = key2[i];
					this.value1[pos1] = value1[i];
				}
			}

			return null;
		} else {
			Object old = this.value1[pos];
			this.value1[pos] = value;
			return (T) old;
		}
	}

	public T remove(long k1, long k2) {
		int mask = this.mask;
		for(int pos = HashCommon.mix(hash(k1, k2)) & mask; ; pos = pos + 1 & mask) {
			if(!this.hasEntry(pos)) {
				return null;
			}
			if(this.isKeyEqualToIncoming(k1, k2, pos)) {
				return remove(pos);
			}
		}
	}

	private T remove(int pos) {
		Object val = this.value1[pos];
		this.value1[pos] = null;
		T value = (T) val;
		this.size--;
		this.shiftKeys(pos);
		return value;
	}

	public T get(long k1, long k2) {
		int mask = this.mask;
		for(int pos = HashCommon.mix(hash(k1, k2)) & mask; ; pos = pos + 1 & mask) {
			if(!this.hasEntry(pos)) {
				return null;
			}
			if(this.isKeyEqualToIncoming(k1, k2, pos)) {
				return (T) this.value1[pos];
			}
		}
	}

	private void shiftKeys(int pos) {
		// Shift entries with the same hash.
		while(true) {
			int last = pos;
			pos = last + 1 & this.mask;
			int currentIndex = pos;

			while(true) {
				if(!this.hasEntry(pos)) {
					this.key1[last] = 0;
					this.key2[last] = 0;
					this.value1[last] = null;
					return;
				}
				int slot = HashCommon.mix(hash(this.key1[pos], this.key2[pos])) & this.mask;
				if(last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
					break;
				}
				pos = (pos + 1) & this.mask;
			}
			this.key1[last] = this.key1[currentIndex];
			this.key2[last] = this.key2[currentIndex];
			this.value1[last] = this.value1[pos];
		}
	}

	private int find(long k1, long k2) {
		int mask = this.mask;
		for(int pos = HashCommon.mix(hash(k1, k2)) & mask; ; pos = pos + 1 & mask) {
			if(!this.hasEntry(pos)) {
				return -(pos + 1);
			}
			if(this.isKeyEqualToIncoming(k1, k2, pos)) {
				return pos;
			}
		}
	}

	private boolean hasEntry(int index) {
		return this.value1[index] != null;
	}

	private boolean isKeyEqualToIncoming(long k1, long k2, int index) {
		return k1 == this.key1[index] && k2 == this.key2[index];
	}

	private void resize(int n) {
		this.mask = n - 1;
		this.maxFill = HashCommon.maxFill(n, this.loadFactor);
		this.key1 = new long[n];
		this.key2 = new long[n];
		this.value1 = new Object[n];
	}

	final class EntrySet extends AbstractSet<Entry<Id.Full, T>> {
		@Override
		public Iterator<Entry<Id.Full, T>> iterator() {
			return IntStream.range(0, size)
				.filter(i -> value1[i] != null)
				.<Entry<Id.Full, T>>mapToObj(MapEntry::new)
				.iterator();
		}

		@Override
		public int size() {
			return size;
		}
	}

	final class MapEntry implements Entry<Id.Full, T> {
		final int index;
		Id.Full key;

		MapEntry(int index) {
			this.index = index;
		}

		@Override
		public Id.Full getKey() {
			Id.Full full = this.key;
			if(full == null) {
				this.key = full = Id.create(key1[index], key2[index]);
			}
			return full;
		}

		@Override
		public T getValue() {
			return (T) value1[index];
		}

		@Override
		public T setValue(T value) {
			T o = (T) value1[index];
			value1[index] = value;
			return o;
		}
	}
}
