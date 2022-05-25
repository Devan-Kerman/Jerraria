package net.devtech.jerraria.world.internal.chunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.world.ChunkLinkingAccess;
import net.devtech.jerraria.world.WorldServer;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.TickingWorld;

public class ChunkGroup {
	// todo unloading reluctance
	final TickingWorld backing;
	public final WorldImpl local;
	int totalTickets;
	public final Long2ObjectMap<Chunk> chunks;

	public ChunkGroup(TickingWorld world) {
		this.backing = world;
		this.local = new WorldImpl();
		chunks = world.doesMaintainOrder() ? new Long2ObjectLinkedOpenHashMap<>() : new Long2ObjectOpenHashMap<>();
	}

	public void ticket() {
		this.totalTickets++;
	}

	public void unticket() {
		this.totalTickets--;
		if(this.totalTickets == 0) {
			this.backing.unloadGroup(this);
		}
	}

	public boolean contains(Chunk chunk) {
		return this.chunks.containsKey(chunk.getId());
	}

	public void add(Chunk chunk) {
		this.chunks.put(chunk.getId(), chunk);
	}

	public void remove(Chunk chunk) {
		this.chunks.remove(chunk.getId());
		if(this.chunks.isEmpty()) {
			this.backing.removeGroup(this);
		}
	}

	public void tick() {
		for(Chunk value : this.chunks.values()) {
			value.tick();
		}
		for(Chunk value : this.chunks.values()) {
			value.moveEntities();
		}
		// todo unload when ticketless
	}

	public boolean runTasks() {
		boolean hasMore = false;
		for(Chunk value : this.chunks.values()) {
			hasMore |= value.runTasks();
		}
		return hasMore;
	}

	public class WorldImpl extends AbstractWorld {
		@Override
		public Chunk getChunk(int x, int y) {
			Chunk chunk = ChunkGroup.this.chunks.get(JMath.combineInts(x, y));
			if(chunk == null) {
				throw new IllegalStateException("chunk does not belong to group, you must link to stack chunk before using it!");
			}
			return chunk;
		}

		Chunk getChunk0(int x, int y) {
			Chunk chunk = ChunkGroup.this.chunks.get(JMath.combineInts(x, y));
			if(chunk == null) {
				chunk = backing.getChunk(x, y);
			}
			return chunk;
		}

		@Override
		public WorldServer getServer() {
			return backing.getServer();
		}

		@Override
		public int sessionId() {
			return backing.sessionId();
		}

		@Override
		public boolean isChunkLoaded(int cx, int cy) {
			return ChunkGroup.this.chunks.containsKey(JMath.combineInts(cx, cy));
		}

		@Override
		public boolean isChunkAccessible(int cx, int cy) {
			return this.isChunkLoaded(cx, cy);
		}

		@Override
		public CompletableFuture<World> linkAndExecute(Consumer<ChunkLinkingAccess> access) {
			LongSet list = new LongOpenHashSet();
			var containsAll = new Object() {
				boolean does = true;
			};
			access.accept((chunkX, chunkY) -> {
				long key = JMath.combineInts(chunkX, chunkY);
				Chunk chunk = ChunkGroup.this.chunks.get(key);
				list.add(key);
				if(chunk == null) {
					containsAll.does = false;
				}
			});
			if(containsAll.does) {
				return CompletableFuture.completedFuture(this);
			} else {
				return ChunkGroup.this.backing.linkAndExecute(acc -> {
					list.forEach(l -> acc.pos(Chunk.getA(l), Chunk.getB(l)));
				});
			}
		}

		@Override
		public ChunkLinkingAccess getUnsafeLinkingAccess(int startChunkX, int startChunkY) {
			Chunk chunk = this.getChunk(startChunkX, startChunkY);
			return (chunkX, chunkY) -> chunk.addLink(this.getChunk0(chunkX, chunkY));
		}

		@Override
		public ChunkLinkingAccess getUnsafeUnlinkingAccess(int startX, int startY) {
			Chunk chunk = this.getChunk(startX, startY);
			return (chunkX, chunkY) -> chunk.removeLink(this.getChunk0(chunkX, chunkY));
		}
	}
}
