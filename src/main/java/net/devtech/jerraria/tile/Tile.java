package net.devtech.jerraria.tile;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.access.Access;
import net.devtech.jerraria.access.internal.AccessImpl;
import net.devtech.jerraria.util.func.TileLinker;

public class Tile {
	public static final Access<TileLinker<TileVariant>> ON_BUILD = new AccessImpl<>(arr -> (linking, convertable, posX, posY) -> {
		for(TileLinker<TileVariant> linker : arr) {
			linker.link(linking, convertable, posX, posY);
		}
	});

	int linkFromX, linkToX, linkFromY, linkToY;
	TileVariant[] cache;
	int defaultIndex;
	List<Property<?, ?>> properties = new ArrayList<>();
	int cacheSize = 1;

	public <E extends Enum<E>> EnumProperty<E> enumProperty(Class<E> type, E defaultValue) {
		return this.addProperty(new EnumProperty<>(type, defaultValue));
	}

	public IntRangeProperty rangeProperty(int from, int to, int defaultValue) {
		return this.addProperty(new IntRangeProperty(from, to, defaultValue));
	}

	public IntRangeProperty rangeProperty(int from, int to) {
		return this.rangeProperty(from, to, from);
	}

	public void setInfluenceRange(int linkFromX, int linkToX, int linkFromY, int linkToY) {
		this.linkFromX = linkFromX;
		this.linkToX = linkToX;
		this.linkFromY = linkFromY;
		this.linkToY = linkToY;
	}

	public <P extends Property<?, ?>> P addProperty(P property) {
		if(this.cache != null) {
			throw new IllegalStateException("""
					Cannot add property after blockstate cache has been initialized,\s
					\tdo not call getDefaultState in your constructor\s
					\tand only call addProperty in your constructor (or field init)""");
		}

		int size = property.values().size();
		if(size == 0) {
			throw new IllegalStateException("Cannot have property with no values!");
		}
		this.cacheSize *= size;
		this.properties.add(property);
		return property;
	}

	public List<Property<?, ?>> getProperties() {
		return this.properties;
	}

	public TileVariant getDefaultState() {
		TileVariant[] cache = this.cache;
		if(cache == null) {
			cache = this.initializeCache();
		}
		return cache[this.defaultIndex];
	}

	// this can be optimized via a map from Property -> int and a table for dimensions maybe
	<T> TileVariant withSub(TileVariant current, Property<T, ?> substitute, T value, boolean notCached) {
		Object2IntOpenHashMap<Property<?, ?>> properties = notCached ? new Object2IntOpenHashMap<>() : null;
		int cacheIndex = 0;
		int mul = 1;
		for(Property<?, ?> property : this.properties) {
			int index = property == substitute ? substitute.indexOfValue(value) : current.values.getInt(property);
			List<?> values = property.values();
			cacheIndex += index * mul;
			mul *= values.size();
			if(notCached) {
				this.addProperty(current, properties, property);
			}
		}
		TileVariant variant = this.cache[cacheIndex];
		if(variant == null) {
			if(properties == null) {
				properties = new Object2IntOpenHashMap<>();
				for(Property<?, ?> property : this.properties) {
					this.addProperty(current, properties, property);
				}
			}
			this.cache[cacheIndex] = variant = new TileVariant(this, properties);
		}
		return variant;
	}

	private void addProperty(TileVariant current, Object2IntOpenHashMap<Property<?, ?>> properties, Property<?, ?> property) {
		properties.put(property, current == null ? property.defaultIndex() : current.values.getInt(property));
	}

	TileVariant[] initializeCache() {
		TileVariant[] cache = new TileVariant[this.cacheSize];
		this.withSub(null, null, null, true);
		this.properties = List.copyOf(this.properties);
		return this.cache = cache;
	}

	static {

	}
}
