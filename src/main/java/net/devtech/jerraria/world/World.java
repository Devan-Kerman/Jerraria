package net.devtech.jerraria.world;

import net.devtech.jerraria.world.chunk.Chunk;

public interface World {
	int LOG2_CHUNK_SIZE = 8;
	int CHUNK_SIZE = 1 << LOG2_CHUNK_SIZE;
	int CHUNK_MASK = CHUNK_SIZE - 1;

	TileLayer layerFor(TileLayers layers);

	default TileLayer fluidLayer() {
		return this.layerFor(TileLayers.FLUID);
	}

	default TileLayer blockLayer() {
		return this.layerFor(TileLayers.BLOCK);
	}

	default TileLayer wallLayer() {
		return this.layerFor(TileLayers.WALL);
	}

	default TileLayer wireLayer() {
		return this.layerFor(TileLayers.WIRE);
	}

	EntityLayer entityLayer();

	Chunk getChunk(int cx, int cy);
}
