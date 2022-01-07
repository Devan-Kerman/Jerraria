package net.devtech.jerraria.world.internal.chunk;

import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.tile.TileData;
import net.devtech.jerraria.tile.TileVariant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public abstract class UnpositionedTileData {
	public static final Registry<Type<?>> REGISTRY = new Registry.Fast<>(null);

	public static <T extends UnpositionedTileData, E> Type<T> createType(Creator<T> creator, Deserializer<T, E> deserializer, Serializer<T, E> serializer) {
		return new Type<>() {
			@Override
			public T read(Chunk chunk, JCElement<?> element) {
				return deserializer.create(this, (JCElement<E>) element);
			}

			@Override
			public T create(TileLayers layers, int localX, int localY, int time) {
				return creator.create(this, layers, localX, localY, time);
			}

			@Override
			public JCElement<?> serialize(UnpositionedTileData data) {
				return serializer.serialize((T) data, this);
			}
		};
	}

	public static <T extends TemporaryTileData, E> Type<T> createAndRegister(Creator<T> creator, Deserializer<T, E> deserializer, Serializer<T, E> serializer, Id.Full id) {
		return REGISTRY.register(id, createType(creator, deserializer, serializer));
	}

	abstract TileLayers getLayer();

	abstract int getLocalX();

	abstract int getLocalY();

	abstract int getCounter();

	abstract void setCounter(int counter);

	abstract Type<?> getType();

	public abstract boolean isCompatible(TileVariant old,
		@Nullable TileData oldData,
		TileVariant new_,
		@Nullable TileData newData);

	int getAndDecrement() {
		int counter = this.getCounter();
		this.setCounter(counter - 1);
		return counter;
	}

	public static abstract class Tickable extends UnpositionedTileData {
		@ApiStatus.OverrideOnly
		abstract void tick(Chunk chunk, World world, TileVariant variant, @Nullable TileData data, TileLayers layers, int x, int y);
	}

	public interface Creator<T extends UnpositionedTileData> {
		T create(Type<? extends T> type, TileLayers layers, int localX, int localY, int time);
	}

	public interface Deserializer<T extends UnpositionedTileData, E> {
		T create(Type<? extends T> type, JCElement<E> element);
	}

	public interface Serializer<T extends UnpositionedTileData, E> {
		JCElement<E> serialize(T data, Type<?> type);
	}

	public static abstract class Type<T extends UnpositionedTileData> extends DefaultIdentifiedObject {
		@Override
		protected final Registry<Type<?>> getValidRegistry() {
			return REGISTRY;
		}

		@ApiStatus.OverrideOnly
		public abstract T read(Chunk chunk, JCElement<?> element);

		@ApiStatus.OverrideOnly
		public abstract T create(TileLayers layers, int localX, int localY, int time);

		@ApiStatus.OverrideOnly
		public abstract JCElement<?> serialize(T data);
	}

	protected long encode() {
		long packedData = this.encodeWithoutCounter() & 0xFFFFFFFFL;
		packedData <<= 32;
		packedData |= this.getCounter();
		return packedData;
	}

	protected int encodeWithoutCounter() {
		int packedData = 0;
		packedData |= this.getLayer().ordinal();
		packedData <<= World.LOG2_CHUNK_SIZE;
		packedData |= this.getLocalX();
		packedData <<= World.LOG2_CHUNK_SIZE;
		packedData |= this.getLocalY();
		return packedData;
	}
}
