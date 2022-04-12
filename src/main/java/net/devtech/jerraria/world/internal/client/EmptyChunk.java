package net.devtech.jerraria.world.internal.client;

import net.devtech.jerraria.jerracode.JCTagView;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.chunk.ChunkGroup;
import net.devtech.jerraria.world.internal.chunk.UnpositionedTileData;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.NotNull;

public class EmptyChunk extends Chunk {
	public EmptyChunk(AbstractWorld world, int chunkX, int chunkY) {
		super(world, chunkX, chunkY);
	}

	public EmptyChunk(AbstractWorld world, int chunkX, int chunkY, JCTagView tag) {
		super(world, chunkX, chunkY, tag);
	}

	@Override
	public void addEntity(Entity entity) {
	}

	@Override
	public <T extends UnpositionedTileData> T schedule(UnpositionedTileData.Type<T> type, TileLayers layer, int x, int y, int duration) {
		return null;
	}

	@Override
	public TileData set(TileLayers layer, int x, int y, TileVariant value) {
		return null;
	}

	@Override
	public void addLink(Chunk chunk) {
	}

	@Override
	public void removeLink(Chunk chunk) {
	}

	@Override
	public void appendToGroup(ChunkGroup group) {
	}

	@Override
	public TileData setData(TileLayers layers, int x, int y, TileData data) {
		return null;
	}

	@Override
	public void execute(@NotNull Runnable command) {
	}
}
