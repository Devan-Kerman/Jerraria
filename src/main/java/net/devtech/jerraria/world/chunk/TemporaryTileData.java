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

public abstract class TemporaryTileData {
	public static final Registry<Type<?>> REGISTRY = new Registry.Fast<>(null);
	public final Type<?> type;
	final TileLayers layer;
	final int localX, localY;
	int counter;

	public static <T extends TemporaryTileData, E> Type<T> createType(Creator<T> creator, Deserializer<T, E> deserializer, Serializer<T, E> serializer) {
		return new Type<>() {
			@Override
			public T read(JCElement<?> element) {
				return deserializer.create(this, (JCElement<E>) element);
			}

			@Override
			public T create(TileLayers layers, int localX, int localY, int time) {
				return creator.create(this, layers, localX, localY, time);
			}

			@Override
			public JCElement<?> serialize(TemporaryTileData data) {
				return serializer.serialize((T) data, this);
			}
		};
	}

	public static <T extends TemporaryTileData, E> Type<T> createAndRegister(Creator<T> creator, Deserializer<T, E> deserializer, Serializer<T, E> serializer, Id.Full id) {
		return REGISTRY.register(id, createType(creator, deserializer, serializer));
	}

	protected TemporaryTileData(Type<?> type, TileLayers layer, int localX, int localY, int time) {
		this.type = type;
		this.layer = layer;
		this.localX = localX;
		this.localY = localY;
		this.counter = time;
	}

	protected TemporaryTileData(Type<?> type, long packedData) {
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

	public static abstract class Type<T extends TemporaryTileData> extends DefaultIdentifiedObject {
		@Override
		protected final Registry<Type<?>> getValidRegistry() {
			return REGISTRY;
		}

		@ApiStatus.OverrideOnly
		public abstract T read(JCElement<?> element);

		@ApiStatus.OverrideOnly
		public abstract T create(TileLayers layers, int localX, int localY, int time);

		@ApiStatus.OverrideOnly
		public abstract JCElement<?> serialize(T data);
	}

	public interface Creator<T extends TemporaryTileData> {
		T create(Type<? extends T> type, TileLayers layers, int localX, int localY, int time);
	}

	public interface Deserializer<T extends TemporaryTileData, E> {
		T create(Type<? extends T> type, JCElement<E> element);
	}

	public interface Serializer<T extends TemporaryTileData, E> {
		JCElement<E> serialize(T data, Type<?> type);
	}
}
