package net.devtech.jerraria.world.internal;

import java.util.Arrays;

import net.devtech.jerraria.world.EntityLayer;
import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public abstract class AbstractWorld implements World {
	final ChunkAccessTileLayer[] tileLayers = new ChunkAccessTileLayer[TileLayers.COUNT];
	final ChunkAccessEntityLayer entityLayer;

	public AbstractWorld() {
		Arrays.setAll(this.tileLayers, value -> new ChunkAccessTileLayer(TileLayers.LAYERS.get(value), this::getChunk));
		this.entityLayer = new ChunkAccessEntityLayer(this::getChunk);
	}

	public abstract Chunk getChunk(int x, int y);

	public void addEntity(Entity entity) {
		Chunk chunk = this.getChunk(entity.getChunkX(), entity.getChunkY());
		chunk.addEntity(entity);
	}

	@Override
	public TileLayer layerFor(TileLayers layers) {
		return this.tileLayers[layers.ordinal()];
	}

	@Override
	public EntityLayer entityLayer() {
		return this.entityLayer;
	}

	public boolean doesMaintainOrder() {
		return false;
	}
}
