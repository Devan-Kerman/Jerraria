package net.devtech.jerraria.world.item;

import java.util.function.Function;

import net.devtech.jerraria.content.Items;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.IdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.data.element.JCElement;

public class IdentityItem extends Item implements Item.Type<Void> {
	Id.Full id;

	protected IdentityItem() {
		super((Item.Type<Void>)null);
		this.setType(this);
	}

	@Override
	public <T extends IdentifiedObject> Id.Full getId(Registry.Fast<T> registry, Function<T, Id.Full> defaultAccess)
		throws UnsupportedOperationException {
		return this.id;
	}

	@Override
	public void setId_(Registry.Fast<?> registry, Id.Full id) throws UnsupportedOperationException {
		Validate.isTrue(registry == Items.REGISTRY,"cannot register " + this + " in custom fast registry, use default registry");
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return super.hashCode0();
	}

	@Override
	public JCElement<Void> serialize(Item instance) {
		return null;
	}

	@Override
	public Item deserialize(JCElement<Void> element) {
		return this;
	}
}
