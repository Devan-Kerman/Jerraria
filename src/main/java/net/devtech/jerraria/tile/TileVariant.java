package net.devtech.jerraria.tile;

import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class TileVariant implements VariantConvertable {
	private static final AtomicInteger ID = new AtomicInteger();

	final Tile owner;
	final Object2IntMap<Property<?, ?>> values;
	int linkFromX, linkToX, linkFromY, linkToY;

	TileVariant(Tile owner, Object2IntMap<Property<?, ?>> values) {
		this.owner = owner;
		this.values = values;
		this.linkFromX = owner.linkFromX;
		this.linkToX = owner.linkToX;
		this.linkFromY = owner.linkFromY;
		this.linkToY = owner.linkToY;
	}

	public Tile getOwner() {
		return this.owner;
	}

	/**
	 * @param property if the property does not belong to the variant, null is returned
	 */
	public <T> T get(Property<T, ?> property) {
		return this.getOrDefault(property, null);
	}

	public <T> T getOrDefault(Property<T, ?> property, T value) {
		int index = this.values.getOrDefault(property, -1);
		if(index == -1) {
			return value;
		} else {
			return property.values().get(index);
		}
	}

	public <T> TileVariant with(Property<T, ?> property, T value) {
		return this.owner.withSub(this, property, value, false);
	}

	public <T> T getOrDefault(Property<T, ?> property) {
		return this.getOrDefault(property, property.defaultValue());
	}

	@Override
	public TileVariant getVariant() {
		return this;
	}
}
