package net.devtech.jerraria.render.api.instanced;

public interface InstanceRelocator<T> {
	void copy(InstanceKey<T> from, InstanceKey<T> to);
}
