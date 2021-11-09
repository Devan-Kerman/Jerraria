package net.devtech.jerraria.registry;

import java.util.HashMap;
import java.util.Map;

import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;

public abstract class Registry<T> {
	final UUID2ObjectMap<T> map = new UUID2ObjectMap<>(1024);
	final T defaultValue;

	public Registry(T value) {
		this.defaultValue = value;
	}

	public <C extends T> C register(Id.Full id, C value) {
		T put = this.map.put(id.packedNamespace, id.getPath(), value);
		Validate.notNull(put, "multiple values for same id " + id + "");
		return value;
	}

	public T getForId(Id id) {
		if(id instanceof Id.Partial) {
			return null;
		} else {
			return this.getForId(id.packedNamespace, id.getPath());
		}
	}

	public T getForId(long a, long b) {
		IdentifierPacker.throwErr("<unknown>", a);
		IdentifierPacker.throwErr("<unknown>", b);
		T t = this.map.get(a, b);
		return t == null ? this.defaultValue : t;
	}

	@Nullable
	public abstract Id.Full getId(T value);

	@Nullable
	protected Id.Full getId0(T value) {
		for(Map.Entry<Id.Full, T> entry : this.map.entrySet()) {
			if(entry.getValue() == value) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static class Fast<T extends IdentifiedObject> extends Registry<T> {
		public Fast(T defaultValue) {
			super(defaultValue);
		}

		@Override
		public Id.@Nullable Full getId(T value) {
			return value.getId(this, super::getId0);
		}

		@Override
		public <C extends T> C register(Id.Full id, C value) {
			value.setId_(this, id);
			return super.register(id, value);
		}
	}

	public static class Default<T> extends Registry<T> {
		final Map<T, Id.Full> reverseMap = new HashMap<>();

		public Default(T value) {
			super(value);
		}

		@Override
		public <C extends T> C register(Id.Full id, C value) {
			Id.Full put = this.reverseMap.put(value, id);
			Validate.isNull(put, "multiple ids for same value! " + value + ": " + id + "/" + put);
			return super.register(id, value);
		}

		@Override
		public Id.@Nullable Full getId(T value) {
			return null;
		}
	}
}
