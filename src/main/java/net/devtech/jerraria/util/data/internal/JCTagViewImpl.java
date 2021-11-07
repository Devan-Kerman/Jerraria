package net.devtech.jerraria.util.data.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.devtech.jerraria.util.data.JCElement;
import net.devtech.jerraria.util.data.JCTagView;
import net.devtech.jerraria.util.data.JCType;
import net.devtech.jerraria.util.data.NativeJCType;

public class JCTagViewImpl implements JCTagView {
	final Map<String, Entry<?, ?>> entries;

	JCTagViewImpl(Map<String, Entry<?, ?>> entries) {
		this.entries = entries;
	}

	@Override
	public Set<String> getKeys() {
		return this.entries.keySet();
	}

	@Override
	public JCElement<?> get(String key) {
		Entry<?, ?> entry = this.entries.get(key);
		return entry == null ? null : entry.convert();
	}

	@Override
	public void forEach(ValuesConsumer consumer) {
		this.entries.forEach((s, entry) -> apply(consumer, s, entry));
	}

	@Override
	public int getInt(String key, int defaultValue) {
		Entry<?, ?> entry = this.entries.get(key);
		if(entry.type == NativeJCType.INT) {
			return (Integer) entry.value;
		} else if(entry.type.nativeType() == NativeJCType.INT) {
			return (Integer) this.getObject(entry);
		} else {
			return defaultValue;
		}
	}

	@Override
	public <T> T get(String key, JCType<T, ?> type, T defaultValue) {
		Entry entry = this.entries.get(key);
		if(entry.type == type) {
			return (T) entry.value;
		} else if(entry.type == type.nativeType()) {
			// deserialize (todo caching, JCType can say it's cachable)
			return ((JCType<T, Object>)type).convertFromNative(entry.value);
		} else {
			return defaultValue;
		}
	}

	private static <T> void apply(ValuesConsumer consumer, String key, Entry<T, ?> entry) {
		try {
			consumer.accept(key, entry.type, entry.value);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private <T, N> N getObject(Entry<T, N> entry) {
		return entry.type.convertToNative(entry.value);
	}

	static final class Entry<T, N> {
		final JCType<T, N> type;
		final T value;

		Entry(JCType<T, N> type, T value) {
			this.type = type;
			this.value = value;
		}

		JCElement<?> convert() {
			return new JCElement<>(type.nativeType(), type.convertToNative(value));
		}
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		return o instanceof JCTagViewImpl view && Objects.equals(this.entries, view.entries);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.entries);
	}
}
