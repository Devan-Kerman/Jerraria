package net.devtech.jerraria.registry;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

public interface IdentifiedObject {
	<T extends IdentifiedObject> Id.Full getId(FastRegistry<T> registry, Function<T, Id.Full> defaultAccess) throws UnsupportedOperationException;

	@ApiStatus.OverrideOnly
	void setId_(FastRegistry<?> registry, Id.Full id) throws UnsupportedOperationException;
}
