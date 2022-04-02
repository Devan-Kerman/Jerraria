package net.devtech.jerraria.jerraria.item;

import net.devtech.jerraria.world.item.IdentityItem;
import net.devtech.jerraria.world.item.Item;

public final class AirItem extends IdentityItem {
	public static final AirItem INSTANCE = new AirItem();
	public final Item.Stack empty = new Stack(0);

	private AirItem() {
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return empty.hashCode();
	}
}
