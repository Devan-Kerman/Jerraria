package net.devtech.jerraria.world;

public interface ChunkLinkingAccess {
	default void pos(int blockPosX, int blockPosY) {
		this.chunk(blockPosX >> World.LOG2_CHUNK_SIZE, blockPosY >> World.LOG2_CHUNK_SIZE);
	}

	void chunk(int chunkX, int chunkY);

	default void range(int fromBlockX, int fromBlockY, int toBlockX, int toBlockY) {
		for(int cx = (fromBlockX >> World.LOG2_CHUNK_SIZE); cx <= (toBlockX >> World.LOG2_CHUNK_SIZE); cx++) {
			for(int cy = (fromBlockY >> World.LOG2_CHUNK_SIZE); cy <= (toBlockY >> World.LOG2_CHUNK_SIZE); cy++) {
				this.chunk(cx, cy);
			}
		}
	}
}
