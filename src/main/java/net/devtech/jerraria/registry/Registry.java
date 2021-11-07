package net.devtech.jerraria.registry;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class Registry<T> {
	final UUID2ObjectMap<T> map = new UUID2ObjectMap<>(1024);
	final T defaultValue;

	public Registry(T value) {
		this.defaultValue = value;
	}

	public T register(Id.Full id, T value) {
		this.map.put(id.packedNamespace, id.getPath(), value);
		return value;
	}

	@Nullable
	public T getForId(Id id) {
		if(id instanceof Id.Partial) {
			return null;
		} else {
			T t = this.map.get(id.packedNamespace, id.getPath());
			return t == null ? this.defaultValue : t;
		}
	}

	@Nullable
	public Id.Full getId(T value) {
		for(Map.Entry<Id.Full, T> entry : this.map.entrySet()) {
			if(entry.getValue() == value) {
				return entry.getKey();
			}
		}
		return null;
	}
}
