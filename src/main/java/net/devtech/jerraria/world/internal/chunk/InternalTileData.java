package net.devtech.jerraria.world.internal.chunk;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class InternalTileData extends UnpositionedTileData.Tickable {
	static final Type TYPE = new Type();
	static {
		REGISTRY.register(Id.createFull("jerraria", "tile_data"), TYPE);
	}

	TileLayers layer;
	int absX, absY;

	static class Type extends UnpositionedTileData.Type<InternalTileData> {
		@Override
		public InternalTileData read(Chunk chunk, JCElement<?> element) {
			return chunk.data.get(element.castTo(NativeJCType.INT));
		}

		@Override
		public InternalTileData create(TileLayers layers, int localX, int localY, int time) {
			throw new UnsupportedOperationException("cannot create");
		}

		@Override
		public JCElement<?> serialize(InternalTileData data) {
			return JCElement.create(NativeJCType.INT, Chunk.getIndex(data.getLayer(), data.getLocalX(), data.getLocalY()));
		}
	}

	@Override
	@ApiStatus.Internal
	public final void tick(Chunk chunk,
		World world,
		TileVariant variant,
		@Nullable TileData data,
		TileLayers layers,
		int x,
		int y) {
		variant.tickData(world, data, layers, x, y);
	}

	@Override
	TileLayers getLayer() {
		return layer;
	}

	@Override
	int getLocalX() {
		return absX & World.CHUNK_MASK;
	}

	@Override
	int getLocalY() {
		return absY & World.CHUNK_MASK;
	}

	@Override
	int getCounter() {
		return Integer.MAX_VALUE;
	}

	@Override
	void setCounter(int counter) {
	}

	@Override
	UnpositionedTileData.Type<?> getType() {
		return null; // todo
	}

	@Override
	public boolean isCompatible(TileVariant old,
		@Nullable TileData oldData,
		TileVariant new_,
		@Nullable TileData newData) {
		return oldData == newData;
	}
}
