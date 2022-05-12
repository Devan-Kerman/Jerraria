package net.devtech.jerraria.registry;

import java.util.function.Function;

import net.devtech.jerraria.util.Id;
import org.jetbrains.annotations.ApiStatus;

public interface IdentifiedObject {
	<T extends IdentifiedObject> Id.Full getId(Registry.Fast<T> registry, Function<T, Id.Full> defaultAccess) throws UnsupportedOperationException;

	@ApiStatus.OverrideOnly
	void setId_(Registry.Fast<?> registry, Id.Full id) throws UnsupportedOperationException;
}
