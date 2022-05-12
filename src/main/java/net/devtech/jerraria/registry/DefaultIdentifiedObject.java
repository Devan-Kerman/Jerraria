package net.devtech.jerraria.registry;

import java.util.function.Function;

import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;

public abstract class DefaultIdentifiedObject implements IdentifiedObject {
	Id.Full id;

	@Override
	public <T extends IdentifiedObject> Id.Full getId(Registry.Fast<T> registry, Function<T, Id.Full> defaultAccess)
		throws UnsupportedOperationException {
		return this.id;
	}

	@Override
	public void setId_(Registry.Fast<?> registry, Id.Full id) throws UnsupportedOperationException {
		Validate.isTrue(registry == this.getValidRegistry(), "cannot register "+this+" in custom fast registry, use default registry");
		this.id = id;
	}

	protected abstract Registry<?> getValidRegistry();
}
