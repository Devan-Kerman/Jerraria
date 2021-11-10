package net.devtech.jerraria.world.tile;

import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import org.jetbrains.annotations.Nullable;

public final class TileVariant implements VariantConvertable {
	final Tile owner;
	final Object2IntMap<Property<?, ?>> values;
	final int cacheIndex;
	int linkFromX, linkToX, linkFromY, linkToY;

	TileVariant(Tile owner, Object2IntMap<Property<?, ?>> values, int index) {
		this.owner = owner;
		this.values = values;
		this.linkFromX = owner.linkFromX;
		this.linkToX = owner.linkToX;
		this.linkFromY = owner.linkFromY;
		this.linkToY = owner.linkToY;
		this.cacheIndex = index;
	}

	public boolean isCompatible(TileData data) {
		return this.owner.isCompatible(this, data);
	}

	public Tile getOwner() {
		return this.owner;
	}

	public boolean hasBlockData() {
		// todo add access api support
		return this.owner.hasBlockData(this);
	}

	public TileData createData() {
		return this.owner.create(this);
	}

	public boolean doesTick(World world, TileData data, TileLayers layers, int x, int y) {
		return this.getOwner().doesDataTick(world, this, data, layers, x, y);
	}

	public void tickData(World world, TileData data, TileLayers layers, int x, int y) {
		this.getOwner().tickData(world, this, data, layers, x, y);
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.owner);
		builder.append('[');
		Iterator<Property<?, ?>> iterator = this.owner.getProperties().iterator();
		while(iterator.hasNext()) {
			Property<?, ?> property = iterator.next();
			builder.append(property.getName());
			builder.append('=');
			builder.append(this.get(property));
			if(iterator.hasNext()) {
				builder.append(',');
			}
		}
		builder.append(']');
		return builder.toString();
	}
}
