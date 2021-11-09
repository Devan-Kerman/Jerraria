package net.devtech.jerraria.world.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.chunk.Chunk;
import net.devtech.jerraria.world.chunk.ChunkGroup;
import org.jetbrains.annotations.VisibleForTesting;

public abstract class TickingWorld extends AbstractWorld implements World {
	final Set<ChunkGroup> groups = new HashSet<>();
	final Set<Chunk> toRelink;
	final Executor executor;
	boolean maintainOrder;

	protected TickingWorld(Executor executor, boolean doesMaintainOrder) {
		this.executor = executor;
		this.maintainOrder = doesMaintainOrder;
		this.toRelink = doesMaintainOrder ? new LinkedHashSet<>() : new HashSet<>();
	}

	public void requiresRelinking(Chunk chunk) {
		this.toRelink.add(chunk);
	}

	public void removeGroup(ChunkGroup group) {
		this.groups.remove(group);
	}

	public abstract void unloadGroup(ChunkGroup group);

	public void createGroups() {
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

	public void tick() {
		this.createGroups();
		List<CompletableFuture<Void>> ticks = new ArrayList<>();
		for(ChunkGroup group : this.groups) {
			ticks.add(CompletableFuture.runAsync(group::tick, this.executor));
		}
		ticks.forEach(CompletableFuture::join);
	}

	public boolean doesMaintainOrder() {
		return this.maintainOrder;
	}
}
