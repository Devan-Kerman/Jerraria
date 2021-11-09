package net.devtech.jerraria.world.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.content.Tiles;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.IdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.access.Access;
import net.devtech.jerraria.util.access.func.FuncFinder;
import net.devtech.jerraria.util.access.internal.AccessImpl;
import net.devtech.jerraria.util.access.priority.PriorityKey;
import net.devtech.jerraria.util.data.JCTagView;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.world.internal.ChunkIOUtil;
import net.devtech.jerraria.world.tile.func.TileProperty;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Long Live the Tile
 */
public class Tile implements IdentifiedObject {
	public static final Access<TileProperty<Boolean>> HAS_BLOCK_ENTITY = new AccessImpl<>(ArrayFunc.builder()
		.retIfNN(FuncFinder.onlyAbstract())
		.buildInfer());
	private static final Logger LOGGER = Logger.getLogger("Tile");

	static {
		HAS_BLOCK_ENTITY.andThen(PriorityKey.DEFAULT, TileVariant::hasBlockData);
	}

	int linkFromX, linkToX, linkFromY, linkToY;
	TileVariant[] cache;
	int defaultIndex;
	List<Property<?, ?>> properties = new ArrayList<>();
	int cacheSize = 1;
	boolean hasBlockEntity;
	Id.Full id;
	TileVariantCacheInitializationStackTrace variantTableInitializationStacktrace;

	public TileVariant getDefaultVariant() {
		var cache = this.initializeCache("getDefaultState");
		return cache[this.defaultIndex];
	}

	// properties

	public <E extends Enum<E>> EnumProperty<E> enumProperty(String name, E defaultValue) {
		return this.addProperty(new EnumProperty<>(name, defaultValue.getDeclaringClass(), defaultValue));
	}

	public IntRangeProperty rangeProperty(String name, int from, int to, int defaultValue) {
		return this.addProperty(new IntRangeProperty(name, from, to, defaultValue));
	}

	public IntRangeProperty rangeProperty(String name, int from, int to) {
		return this.rangeProperty(name, from, to, from);
	}

	public List<Property<?, ?>> getProperties() {
		this.initializeCache("getProperties");
		return this.properties;
	}

	public <P extends Property<?, ?>> P addProperty(P property) {
		if(this.cache != null) {
			throw new IllegalStateException("""
				Cannot add property after blockstate cache has been initialized,
				\tdo not call getDefaultState/getProperties before addProperty
				\tand only call addProperty in your constructor (or field init)""",
				this.variantTableInitializationStacktrace);
		}
		String name = property.getName();
		Objects.requireNonNull(name, "property name cannot be null!");
		if(ChunkIOUtil.RESERVED_ID.equals(name)) {
			throw new IllegalArgumentException("Cannot name tile property '"+ChunkIOUtil.RESERVED_ID+"'");
		}

		int size = property.values().size();
		if(size == 0) {
			throw new IllegalStateException("Cannot have property with no values!");
		}
		this.cacheSize *= size;
		this.properties.add(property);
		return property;
	}

	// block data

	public final void enableBlockData() {
		this.hasBlockEntity = true;
	}

	@ApiStatus.OverrideOnly
	protected boolean hasBlockData(TileVariant variant) {
		return this.hasBlockEntity;
	}

	@ApiStatus.OverrideOnly
	protected TileData create(TileVariant variant) {
		if(this.hasBlockData(variant)) {
			throw new UnsupportedOperationException("Tile has blockdata for " + variant + " but doesn't override " +
			                                        "TileData#create!");
		}
		return null;
	}

	@ApiStatus.OverrideOnly
	protected void write(TileData data, TileVariant variant, JCTagView.Builder builder) {
		if(this.hasBlockData(variant)) {
			throw new UnsupportedOperationException("Tile has blockdata for " + variant + " but doesn't override " +
			                                        "TileData#write!");
		}
	}

	/**
	 * It is good practice to make this method delegate to the TileData's constructor
	 */
	@Nullable
	@ApiStatus.OverrideOnly
	protected TileData read(TileVariant variant, JCTagView view) {
		if(this.hasBlockData(variant)) {
			throw new UnsupportedOperationException("Tile has blockdata for " + variant + " but doesn't override " +
			                                        "TileData#read!");
		}
		return null;
	}

	public boolean isCompatible(TileVariant variant, TileData data) {
		if(this.hasBlockEntity) {
			return variant.owner == this;
		} else if(this.hasBlockData(variant)) {
			return !(data instanceof GenericTileData);
		} else {
			return false;
		}
	}

	/**
	 * todo implement + granular control
	 * The range relative to the tile in which the tile can modify blocks
	 */
	public void setInfluenceRange(int linkFromX, int linkToX, int linkFromY, int linkToY) {
		this.linkFromX = linkFromX;
		this.linkToX = linkToX;
		this.linkFromY = linkFromY;
		this.linkToY = linkToY;
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

	private void addProperty(TileVariant current,
		Object2IntOpenHashMap<Property<?, ?>> properties,
		Property<?, ?> property) {
		properties.put(property, current == null ? property.defaultIndex() : current.values.getInt(property));
	}


	TileVariant[] initializeCache(String apiName) {
		TileVariant[] cache = this.cache;
		if(cache == null) {
			this.variantTableInitializationStacktrace = new TileVariantCacheInitializationStackTrace(apiName);
			this.cache = cache = new TileVariant[this.cacheSize];
			this.withSub(null, null, null, true);
			this.properties = List.copyOf(this.properties);
		}
		return cache;
	}

	@Override
	public <T extends IdentifiedObject> Id.Full getId(Registry.Fast<T> registry, Function<T, Id.Full> access)
		throws UnsupportedOperationException {
		if(this.id == null) {
			throw new IllegalArgumentException(this + " not registered!");
		} else if(registry != Tiles.REGISTRY) {
			throw new UnsupportedOperationException("Cannot use custom FastRegistry on Tile!");
		}
		return id;
	}

	@Override
	public void setId_(Registry.Fast<?> registry, Id.Full id) throws UnsupportedOperationException {
		if(registry == Tiles.REGISTRY) {
			this.id = id;
		} else {
			throw new UnsupportedOperationException("Cannot use custom FastRegistry on Tile!");
		}
	}

	static final class TileVariantCacheInitializationStackTrace extends RuntimeException {
		public TileVariantCacheInitializationStackTrace(String methodName) {
			super("call to method " + methodName + " forced initialization of TileVariant table");
		}
	}
}
