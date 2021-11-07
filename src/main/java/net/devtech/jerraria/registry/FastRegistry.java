package net.devtech.jerraria.registry;

import org.jetbrains.annotations.Nullable;

public class FastRegistry<T extends IdentifiedObject> extends Registry<T> {
	public FastRegistry(T defaultValue) {
		super(defaultValue);
	}

	@Override
	public Id.@Nullable Full getId(T value) {
		return value.getId(this, super::getId);
	}

	@Override
	public T register(Id.Full id, T value) {
		value.setId_(this, id);
		return super.register(id, value);
	}
}
