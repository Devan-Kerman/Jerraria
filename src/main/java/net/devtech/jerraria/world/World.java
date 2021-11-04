package net.devtech.jerraria.world;

public interface World {
	int LOG2_CHUNK_SIZE = 8;
	int CHUNK_SIZE = 1 << LOG2_CHUNK_SIZE;
	int CHUNK_MASK = CHUNK_SIZE - 1;

	TileLayer blockLayer();

	TileLayer wallLayer();

	TileLayer wireLayer();

	EntityLayer entityLayer();
}
