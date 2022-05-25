package net.devtech.jerraria.world.internal;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.chunk.ChunkGroup;
import net.devtech.jerraria.world.ChunkLinkingAccess;

public abstract class TickingWorld extends AbstractWorld implements World {
	final Set<ChunkGroup> groups = newSetFromMap(new ConcurrentHashMap<>());
	final Set<Chunk> toRelink;
	final Executor executor;
	boolean maintainOrder;

	protected TickingWorld(Executor executor, boolean maintainOrder) {
		this.executor = executor;
		this.maintainOrder = maintainOrder;
		this.toRelink = maintainOrder ? synchronizedSet(new LinkedHashSet<>()) :
		                newSetFromMap(new ConcurrentHashMap<>());
	}

	public void requiresRelinking(Chunk chunk) {
		this.toRelink.add(chunk);
	}

	public void removeGroup(ChunkGroup group) {
		this.groups.remove(group);
	}

	public abstract void unloadGroup(ChunkGroup group);

	public abstract void unloadIndividualChunk(Chunk chunk);

	public void tick() {
		this.createGroups();
		List<CompletableFuture<Void>> ticks = new ArrayList<>();
		for(ChunkGroup group : this.groups) {
			ticks.add(CompletableFuture.runAsync(group::tick, this.executor));
		}
		CompletableFuture.allOf(ticks.toArray(CompletableFuture[]::new)).join();

		// execute immediate tasks
		AtomicBoolean hasTasks = new AtomicBoolean();
		do {
			ticks.clear();
			hasTasks.set(false);
			this.createGroups();
			for(ChunkGroup group : this.groups) {
				ticks.add(CompletableFuture.runAsync(() -> {
					boolean val = group.runTasks();
					hasTasks.compareAndSet(false, val);
				}, this.executor));
			}
			CompletableFuture.allOf(ticks.toArray(CompletableFuture[]::new)).join();
		} while(hasTasks.get());

		// autosaving and shutdown should run here, after immediate tasks
	}

	@Override
	public boolean doesMaintainOrder() {
		return this.maintainOrder;
	}

	@Override
	public CompletableFuture<World> linkAndExecute(Consumer<ChunkLinkingAccess> access) {
		var ref = new Object() {
			Chunk chunk;
		};
		LongSet visited = new LongOpenHashSet();
		List<Chunk> chunks = new ArrayList<>();
		access.accept((chunkX, chunkY) -> {
			long key = JMath.combineInts(chunkX, chunkY);
			if(!visited.add(key) || (ref.chunk != null && ref.chunk.getId() == key)) {
				return;
			}
			Chunk chunk = this.getChunk(chunkX, chunkY);

			if(ref.chunk == null) {
				chunk.attachToGroup();
				ref.chunk = chunk;
			} else {
				ref.chunk.addLink(chunk);
				chunks.add(chunk);
			}
		});

		return CompletableFuture.supplyAsync(() -> {
			ChunkGroup group = ref.chunk.getBlockGroup();
			return (World) group.local;
		}, ref.chunk).whenComplete((world, throwable) -> {
			for(Chunk chunk : chunks) {
				ref.chunk.removeLink(chunk);
			}
			if(throwable != null) {
				throw Validate.rethrow(throwable);
			}
		});
	}

	protected void createGroups() {
		while(!this.toRelink.isEmpty()) {
			Chunk chunk = this.toRelink.iterator().next();
			ChunkGroup group = new ChunkGroup(this);
			chunk.appendToGroup(group);
			for(Chunk value : group.chunks.values()) {
				this.toRelink.remove(value);
			}
			this.groups.add(group);
		}
	}

	@Override
	public ChunkLinkingAccess getUnsafeLinkingAccess(int startChunkX, int startChunkY) {
		Chunk chunk = this.getChunk(startChunkX, startChunkY);
		return (chunkX, chunkY) -> chunk.addLink(this.getChunk(chunkX, chunkY));
	}

	@Override
	public ChunkLinkingAccess getUnsafeUnlinkingAccess(int startX, int startY) {
		Chunk chunk = this.getChunk(startX, startY);
		return (chunkX, chunkY) -> chunk.removeLink(this.getChunk(chunkX, chunkY));
	}
}
