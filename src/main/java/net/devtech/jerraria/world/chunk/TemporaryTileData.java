package net.devtech.jerraria.world.chunk;

import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public abstract class TemporaryTileData<T> {
	public static final Registry<Type<?>> REGISTRY = new Registry.Fast<>(null);
	public final Type<T> type;
	final TileLayers layer;
	final int localX, localY;
	int counter;

	public static <T> Type<T> createType(Creator<T> creator, Deserializer<T> deserializer) {
		return new Type<>() {
			@Override
			public TemporaryTileData<T> read(JCElement<T> element) {
				return deserializer.create(this, element);
			}

			@Override
			public TemporaryTileData<T> create(TileLayers layers, int localX, int localY, int time) {
				return creator.create(this, layers, localX, localY, time);
			}
		};
	}

	public static <T> Type<T> createAndRegister(Creator<T> creator, Deserializer<T> deserializer, Id.Full id) {
		return REGISTRY.register(id, createType(creator, deserializer));
	}

	protected TemporaryTileData(Type<T> type, TileLayers layer, int localX, int localY, int time) {
		this.type = type;
		this.layer = layer;
		this.localX = localX;
		this.localY = localY;
		this.counter = time;
	}

	protected TemporaryTileData(Type<T> type, long packedData) {
		this.type = type;
		this.layer = TileLayers.LAYERS.get((int) (packedData & TileLayers.COUNT_MASK));
		packedData >>= TileLayers.COUNT_LOG2;
		this.localX = (int) (World.CHUNK_MASK & packedData);
		packedData >>= World.LOG2_CHUNK_SIZE;
		this.localY = (int) (World.CHUNK_MASK & packedData);
		packedData >>= World.LOG2_CHUNK_SIZE;
		this.counter = (int) (packedData);
	}

	/**
	 * Set the number of ticks until the task must be executed
	 */
	public void setTime(int counter) {
		this.counter = counter;
	}

	protected abstract void onInvalidated(Chunk chunk, TileVariant variant, @Nullable TileData data, World world, int x, int y);

	protected abstract boolean isCompatible(TileVariant old, TileVariant new_);

	@ApiStatus.OverrideOnly
	public abstract JCElement<T> write();

	protected long encode() {
		long packedData = 0;
		packedData |= this.layer.ordinal();
		packedData <<= TileLayers.COUNT_LOG2;
		packedData |= this.localX;
		packedData <<= World.LOG2_CHUNK_SIZE;
		packedData |= this.localY;
		packedData <<= World.LOG2_CHUNK_SIZE + 14;
		packedData |= this.counter;
		return packedData;
	}

	public static abstract class Type<T> extends DefaultIdentifiedObject {
		@Override
		protected final Registry<Type<?>> getValidRegistry() {
			return REGISTRY;
		}

		@ApiStatus.OverrideOnly
		public abstract TemporaryTileData<T> read(JCElement<T> element);

		@ApiStatus.OverrideOnly
		public abstract TemporaryTileData<T> create(TileLayers layers, int localX, int localY, int time);

	}

	public interface Creator<T> {
		TemporaryTileData<T> create(Type<T> type, TileLayers layers, int localX, int localY, int time);
	}

	public interface Deserializer<T> {
		TemporaryTileData<T> create(Type<T> type, JCElement<T> element);
	}
}
