package net.devtech.jerraria.world.tile;

import static java.util.Objects.requireNonNullElse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.IdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.hacks.Func;
import net.devtech.jerraria.util.TriState;
import net.devtech.jerraria.util.access.Access;
import net.devtech.jerraria.util.access.func.FuncFinder;
import net.devtech.jerraria.util.access.internal.AccessImpl;
import net.devtech.jerraria.util.access.priority.PriorityKey;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.func.ArrayFunc;
import net.devtech.jerraria.util.math.Vec2d;
import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.chunk.ChunkCodec;
import net.devtech.jerraria.world.tile.func.TileProperty;
import net.devtech.jerraria.world.tile.render.TileRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Long Live the Tile
 */
public abstract class Tile implements IdentifiedObject {
	public static final Access<TileProperty<Boolean>> HAS_BLOCK_ENTITY = new AccessImpl<>(ArrayFunc.builder()
		.retIfNN(FuncFinder.onlyAbstract())
		.buildInfer());

	private static final Logger LOGGER = Logger.getLogger("Tile");

	static {
		HAS_BLOCK_ENTITY.andThen(PriorityKey.DEFAULT, TileVariant::hasBlockData);
	}

	TriState doesTileDataTick;

	int linkFromX, linkToX, linkFromY, linkToY;
	TileVariant[] cache;
	int defaultIndex;
	List<EnumerableProperty<?, ?>> properties = new ArrayList<>();
	int cacheSize = 1;
	boolean hasBlockEntity;
	Id.Full id;
	TileVariantCacheInitializationStackTrace variantTableInitializationStacktrace;

	public TileVariant getDefaultVariant() {
		var cache = this.initializeCache("getDefaultState");
		return cache[this.defaultIndex];
	}

	public abstract TileRenderer getRenderer(TileVariant variant);

	/**
	 * @see TileLayer#scheduleTick(int, int, int)
	 */
	public void onScheduledTick(World world, TileVariant variant, @Nullable TileData data, TileLayers layer, int x, int y) {
	}

	/**
	 * @param entity the entity that is colliding with this tile
	 * @param projectedTrajectory the direction the entity will go if there is no collision
	 * @param world the world the block is in
	 * @return null to cancel further block/entity collision handling, this means the block handles entity movement
	 *  for example if the block is a portal, the block will just update the entity position and return null
	 */
	public Vec2d handleCollision(Entity entity, Vec2d projectedTrajectory, World world, TileVariant variant, @Nullable TileData data, TileLayers layer, int x, int y) {
		// todo implement basic collision
		return projectedTrajectory;
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

	public List<EnumerableProperty<?, ?>> getProperties() {
		this.initializeCache("getProperties");
		return this.properties;
	}

	public <P extends EnumerableProperty<?, ?>> P addProperty(P property) {
		if(this.cache != null) {
			throw new IllegalStateException(
				"""
					Cannot add property after blockstate cache has been initialized,
					\tdo not call getDefaultState/getProperties before addProperty
					\tand only call addProperty in your constructor (or field init)""",
				this.variantTableInitializationStacktrace);
		}
		String name = property.getName();
		Objects.requireNonNull(name, "property name cannot be null!");
		if(ChunkCodec.RESERVED_ID.equals(name)) {
			throw new IllegalArgumentException("Cannot name tile property '" + ChunkCodec.RESERVED_ID + "'");
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
	public boolean doesDataTick(World world, TileVariant variant, @NotNull TileData data, TileLayers layers, int x, int y) {
		TriState dat = this.doesTileDataTick;
		switch(dat) {
			case UNIT -> {
				boolean overrides = Func.get(Tile::tickData).getDeclaringClass() != Tile.class;
				this.doesTileDataTick = overrides ? TriState.TRUE : TriState.FALSE;
				return overrides;
			}
			case TRUE -> {
				return true;
			}
			case FALSE -> {
				return false;
			}
		}
		return false;
	}

	@ApiStatus.OverrideOnly
	public void tickData(World world, TileVariant variant, @NotNull TileData data, TileLayers layers, int x, int y) {
	}

	public final void enableBlockData() {
		this.hasBlockEntity = true;
	}

	public boolean isCompatible(TileVariant variant, TileData data, TileVariant oldVariant) {
		if(this.hasBlockEntity) {
			return variant.owner == this;
		} else if(this.hasBlockData(variant)) {
			return !(data instanceof GenericTileData);
		} else {
			return false;
		}
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
	protected JCElement<?> write(TileData data, TileVariant variant) {
		if(this.hasBlockData(variant)) {
			throw new UnsupportedOperationException("Tile has blockdata for " + variant + " but doesn't override " +
			                                        "TileData#write!");
		}
		return null;
	}

	/**
	 * It is good practice to make this method delegate to the TileData's constructor
	 */
	@Nullable
	@ApiStatus.OverrideOnly
	protected TileData read(TileVariant variant, JCElement<?> view) {
		if(this.hasBlockData(variant)) {
			throw new UnsupportedOperationException("Tile has blockdata for " + variant + " but doesn't override " +
			                                        "TileData#read!");
		}
		return null;
	}

	// this can be optimized via stack map from Property -> int and stack table for dimensions maybe
	<T> TileVariant withSub(TileVariant current, EnumerableProperty<T, ?> substitute, T value, boolean notCached) {
		Object2IntOpenHashMap<EnumerableProperty<?, ?>> properties = notCached ? new Object2IntOpenHashMap<>() : null;
		int cacheIndex = 0;
		int mul = 1;
		for(EnumerableProperty<?, ?> property : this.properties) {
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
				for(EnumerableProperty<?, ?> property : this.properties) {
					this.addProperty(current, properties, property);
				}
			}
			this.cache[cacheIndex] = variant = new TileVariant(this, properties, cacheIndex);
		}
		return variant;
	}

	void addProperty(TileVariant current, Object2IntOpenHashMap<EnumerableProperty<?, ?>> properties, EnumerableProperty<?, ?> property) {
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

	static final class TileVariantCacheInitializationStackTrace extends RuntimeException {
		public TileVariantCacheInitializationStackTrace(String methodName) {
			super("call to method " + methodName + " forced initialization of TileVariant table");
		}
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

	@Override
	public String toString() {
		String name = this.getClass().getSimpleName();
		return name + " " + requireNonNullElse(this.id, "unregistered");
	}
}
