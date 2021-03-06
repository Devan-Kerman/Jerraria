package net.devtech.jerraria.world.tile;

import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.render.TileRenderer;

public final class TileVariant implements VariantConvertable {
	final Tile owner;
	final Object2IntMap<EnumerableProperty<?, ?>> values;
	final int cacheIndex;
	int linkFromX, linkToX, linkFromY, linkToY;

	TileVariant(Tile owner, Object2IntMap<EnumerableProperty<?, ?>> values, int index) {
		this.owner = owner;
		this.values = values;
		this.linkFromX = owner.linkFromX;
		this.linkToX = owner.linkToX;
		this.linkFromY = owner.linkFromY;
		this.linkToY = owner.linkToY;
		this.cacheIndex = index;
	}

	public boolean isCompatible(TileData data, TileVariant oldVariant) {
		return this.owner.isCompatible(this, data, oldVariant);
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
	public <T> T get(EnumerableProperty<T, ?> property) {
		return this.getOrDefault(property, null);
	}

	public <T> T getOrDefault(EnumerableProperty<T, ?> property, T value) {
		int index = this.values.getOrDefault(property, -1);
		if(index == -1) {
			return value;
		} else {
			return property.values().get(index);
		}
	}

	public <T> TileVariant with(EnumerableProperty<T, ?> property, T value) {
		return this.owner.withSub(this, property, value, false);
	}

	public <T> T getOrDefault(EnumerableProperty<T, ?> property) {
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
		Iterator<EnumerableProperty<?, ?>> iterator = this.owner.getProperties().iterator();
		while(iterator.hasNext()) {
			EnumerableProperty<?, ?> property = iterator.next();
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

	public TileRenderer getRenderer() {
		return this.owner.getRenderer(this);
	}
}
