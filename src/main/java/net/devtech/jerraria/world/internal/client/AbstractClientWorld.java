package net.devtech.jerraria.world.internal.client;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.devtech.jerraria.world.ChunkLinkingAccess;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.WorldServer;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;

// todo networking
public abstract class AbstractClientWorld extends AbstractWorld {
	final CompletableFuture<World> clientCompleted = CompletableFuture.completedFuture(this);
	final ChunkLinkingAccess nopChunkLinkingAccess = (chunkX, chunkY) -> {};
	final WorldServer clientServer;
	final int sessionId;

	public AbstractClientWorld(WorldServer server, int id) {
		this.clientServer = server;
		this.sessionId = id;
	}

	@Override
	public WorldServer getServer() {
		return this.clientServer;
	}

	@Override
	public int sessionId() {
		return this.sessionId;
	}


	@Override
	public CompletableFuture<World> linkAndExecute(Consumer<ChunkLinkingAccess> access) {
		return this.clientCompleted;
	}

	@Override
	public ChunkLinkingAccess getUnsafeLinkingAccess(int startChunkX, int startChunkY) {
		return this.nopChunkLinkingAccess;
	}

	@Override
	public ChunkLinkingAccess getUnsafeUnlinkingAccess(int startX, int startY) {
		return this.nopChunkLinkingAccess;
	}
}
