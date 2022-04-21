package net.devtech.jerraria.world;


import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

public interface World {
	int LOG2_CHUNK_SIZE = 8;
	int LOG2_CHUNK_QUADRANT_SIZE = LOG2_CHUNK_SIZE - 1;
	int CHUNK_SIZE = 1 << LOG2_CHUNK_SIZE;
	int CHUNK_QUADRANT_SIZE = 1 << LOG2_CHUNK_QUADRANT_SIZE;
	int CHUNK_MASK = CHUNK_SIZE - 1;

	WorldServer getServer();

	/**
	 * Unique id of this world for this game session. Aka: not stable across restarts!
	 */
	int sessionId();

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
	 * @see #isChunkLoaded(int, int)
	 */
	default boolean isBlockLoaded(int bx, int by) {
		return this.isChunkLoaded(bx >> World.LOG2_CHUNK_SIZE, by >> World.LOG2_CHUNK_SIZE);
	}

	/**
	 * @see #isChunkAccessible(int, int)
	 */
	default boolean isBlockAccessible(int bx, int by) {
		return this.isChunkAccessible(bx >> World.LOG2_CHUNK_SIZE, by >> World.LOG2_CHUNK_SIZE);
	}

	/**
	 * Whether the world has a chunk for the given chunk position already.
	 *
	 * This returns false if the chunk is ungenerated/unloaded or not present (client)
	 */
	@ApiStatus.Internal
	boolean isChunkLoaded(int cx, int cy);

	/**
	 * Whether the world has a chunk for the given block position, or can load one for the given position on demand
	 */
	@ApiStatus.Internal
	boolean isChunkAccessible(int cx, int cy);

	/**
	 * Executes the given action on the given chunk's group/thread.
	 *  If the current world contains the given chunk (eg. if the World represents stack ChunkGroup)
	 *  then the action is executed immediately, otherwise it is executed at the end of the world tick
	 */
	default CompletableFuture<World> executeAt(int bx, int by) {
		return this.linkAndExecute(access -> access.pos(bx, by));
	}

	/**
	 * Links the chunk at the given location to the chunk at the other location, and executes the given action on the new group's thread.
	 *  this method is preferable to {@link World#executeAt(int, int)} when synchronous to both the current and targeted chunk are necessary
	 *  If the current world contains the given chunk (eg. if the World represents stack ChunkGroup)
	 *  then the action is executed immediately, otherwise it is executed at the end of the world tick
	 */
	default CompletableFuture<World> linkAndExecute(int fromX, int fromY, int bx, int by) {
		return this.linkAndExecute(access -> {
			access.pos(fromX, fromY);
			access.pos(bx, by);
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
