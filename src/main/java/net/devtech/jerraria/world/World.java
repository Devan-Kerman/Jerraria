package net.devtech.jerraria.world;


import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

	/**
	 * Whether the world has a chunk for the given block position
	 */
	boolean isLoaded(int x, int y);

	/**
	 * Whether the world has a chunk for the given block position, or can load one for the given position on demand
	 */
	boolean canAccessImmediately(int x, int y);

	/**
	 * Executes the given action on the given chunk's group/thread.
	 *  If the current world contains the given chunk (eg. if the World represents stack ChunkGroup)
	 *  then the action is executed immediately, otherwise it is executed at the end of the world tick
	 */
	default CompletableFuture<World> executeAt(int x, int y) {
		return this.linkAndExecute(access -> access.link(x, y));
	}

	/**
	 * Links the chunk at the given location to the chunk at the other location, and executes the given action on the new group's thread.
	 *  this method is preferable to {@link World#executeAt(int, int)} when synchronous to both the current and targeted chunk are necessary
	 *  If the current world contains the given chunk (eg. if the World represents stack ChunkGroup)
	 *  then the action is executed immediately, otherwise it is executed at the end of the world tick
	 */
	default CompletableFuture<World> linkAndExecute(int fromX, int fromY, int x, int y) {
		return this.linkAndExecute(access -> {
			access.link(fromX, fromY);
			access.link(x, y);
		});
	}

	CompletableFuture<World> linkAndExecute(Consumer<ChunkLinkingAccess> access);

	/**
	 * These are very tricky to use, they allow for permanent linking between chunks, they do not keep track of redundant links, therefore
	 *  if you call link on the same chunk 4 times, u must call unlink {@link #getUnsafeUnlinkingAccess(int, int)} on that chunk 4 times as well.
	 *
	 * @param startChunkX the x <b>chunk</b> coordinate that the linking should start from
	 *  you must use the exact same value in {{@link #getUnsafeUnlinkingAccess(int, int)}}!
	 */
	ChunkLinkingAccess getUnsafeLinkingAccess(int startChunkX, int startChunkY);

	ChunkLinkingAccess getUnsafeUnlinkingAccess(int startX, int startY);
}
