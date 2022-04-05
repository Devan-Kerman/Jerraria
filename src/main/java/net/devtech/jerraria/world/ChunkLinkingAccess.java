package net.devtech.jerraria.world;

public interface ChunkLinkingAccess {
	default void link(int blockPosX, int blockPosY) {
		this.linkChunk(blockPosX >> World.LOG2_CHUNK_SIZE, blockPosY >> World.LOG2_CHUNK_SIZE);
	}

	void linkChunk(int chunkX, int chunkY);

	default void linkRange(int fromBlockX, int fromBlockY, int toBlockX, int toBlockY) {
		for(int cx = (fromBlockX >> World.LOG2_CHUNK_SIZE); cx <= (toBlockX >> World.LOG2_CHUNK_SIZE); cx++) {
			for(int cy = (fromBlockY >> World.LOG2_CHUNK_SIZE); cy <= (toBlockY >> World.LOG2_CHUNK_SIZE); cy++) {
				this.linkChunk(cx, cy);
			}
		}
	}
}
