package net.devtech.jerraria.world.internal;

import java.util.Arrays;

import net.devtech.jerraria.world.EntityLayer;
import net.devtech.jerraria.world.TileLayer;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.chunk.Chunk;

public abstract class AbstractWorld implements World {
	final ChunkAccessTileLayer[] tileLayers = new ChunkAccessTileLayer[TileLayers.COUNT];

	public AbstractWorld() {
		Arrays.setAll(this.tileLayers, value -> new ChunkAccessTileLayer(TileLayers.LAYERS.get(value), this::getChunk));
	}

	protected abstract Chunk getChunk(int x, int y);

	@Override
	public TileLayer layerFor(TileLayers layers) {
		return this.tileLayers[layers.ordinal()];
	}

	@Override
	public EntityLayer entityLayer() {
		throw new UnsupportedOperationException("mald");
	}
}