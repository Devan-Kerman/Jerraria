package net.devtech.jerraria.world.internal.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.content.Tiles;
import net.devtech.jerraria.util.data.JCTagView;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.entity.BaseEntity;
import net.devtech.jerraria.entity.EntityInternal;
import net.devtech.jerraria.world.internal.TickingWorld;
import net.devtech.jerraria.tile.TileData;
import net.devtech.jerraria.tile.TileVariant;
import org.jetbrains.annotations.NotNull;

public class Chunk implements Executor {
	final TickingWorld world;
	final int chunkX, chunkY;
	/**
	 * A flattened 3 dimensional array of each tile layer
	 */
	final TileVariant[] variants = new TileVariant[World.CHUNK_SIZE * World.CHUNK_SIZE * TileLayers.COUNT];
	final Int2ObjectMap<TileData> data;
	final List<UnpositionedTileData> actions;
	final Object2IntMap<Chunk> links;
	final List<Runnable> immediateTasks = new ArrayList<>();
	final Set<BaseEntity> entities;
	final Iterable<BaseEntity> filteredEntitiesView;

	List<IntLongPair> unresolved;

	int ticketCount;
	ChunkGroup group;

	public Chunk(TickingWorld world, int chunkX, int chunkY) {
		Arrays.fill(this.variants, Tiles.AIR.getDefaultVariant());
		this.data = new Int2ObjectOpenHashMap<>();
		this.actions = new ArrayList<>();
		this.links = world.doesMaintainOrder() ? new Object2IntLinkedOpenHashMap<>() : new Object2IntOpenHashMap<>();
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.entities = new HashSet<>();
		//noinspection StaticPseudoFunctionalStyleMethod
		this.filteredEntitiesView = Iterables.filter(this.entities, BaseEntity::inWorld);
	}

	public Chunk(TickingWorld world, int chunkX, int chunkY, JCTagView tag) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		ChunkCodec.populateTiles(variants, tag.get("tiles", NativeJCType.POOLED_TAG_LIST));
		this.data = ChunkCodec.deserializeData(
			chunkX,
			chunkY,
			this.variants,
			tag.get("data", NativeJCType.INT_ANY_LIST));
		this.unresolved = tag.get("links", NativeJCType.INT_LONG_LIST);
		this.links = new Object2IntOpenHashMap<>(unresolved.size());
		this.actions = ChunkCodec.deserializeTemporaryData(this, tag.get("actions", NativeJCType.ID_ANY_LIST));
		this.entities = ChunkCodec.deserializeEntities(world, tag.get("entities", NativeJCType.ENTITIES));
		//noinspection StaticPseudoFunctionalStyleMethod
		this.filteredEntitiesView = Iterables.filter(this.entities, BaseEntity::inWorld);
		for(BaseEntity entity : this.entities) {
			EntityInternal.setHomeChunk(entity, this);
		}
	}

	public void addEntity(BaseEntity entity) {
		this.entities.add(entity);
		EntityInternal.setHomeChunk(entity, this);
	}

	public JCTagView write() {
		JCTagView.Builder tag = JCTagView.builder();
		tag.put("tiles", NativeJCType.POOLED_TAG_LIST, ChunkCodec.writeTiles(this.variants));
		tag.put("data", NativeJCType.INT_ANY_LIST, ChunkCodec.serializeData(this.variants, this.data));
		tag.put("actions", NativeJCType.ID_ANY_LIST, ChunkCodec.serializeTemporaryData(this.actions));
		tag.put("links", NativeJCType.INT_LONG_LIST, ChunkCodec.serializeLinks(this.links));
		tag.put("entities", NativeJCType.ENTITIES, ChunkCodec.serializeEntities(this.entities));
		return tag;
	}

	public <T extends UnpositionedTileData> T schedule(TemporaryTileData.Type<T> type,
		TileLayers layer,
		int x,
		int y,
		int duration) {
		T data = type.create(layer, x, y, duration);
		this.actions.add(data);
		return data;
	}

	public void resolve() {
		this.links.putAll(ChunkCodec.deserializeLinks(this.unresolved, this.world));
	}

	public List<IntLongPair> getUnresolved() {
		return this.unresolved;
	}

	public void ticket() {
		this.ticketCount++;
		if(this.group != null) {
			this.group.ticket();
		}
	}

	public void unticket() {
		this.ticketCount--;
		if(this.group != null) {
			this.group.unticket();
		} else if(this.ticketCount == 0) {
			this.world.unloadIndividualChunk(this);
		}
	}

	// todo less memory intensive linking system?

	public void removeLink(Chunk chunk) {
		if(chunk == this) return;
		chunk.links.computeIntIfPresent(this, (c, i) -> i - 1);
		if(this.links.computeIntIfPresent(chunk, (c, i) -> i - 1) <= 0) {
			this.world.requiresRelinking(this);
			this.group = null;
		}
	}

	public void addLink(Chunk chunk) {
		if(chunk == this) return;
		chunk.links.mergeInt(this, 1, (c, i) -> i + 1);
		if(this.links.mergeInt(chunk, 1, (c, i) -> i + 1) == 1) {
			this.world.requiresRelinking(this);
		}
	}

	/**
	 * can only be called synchronously by world
	 */
	public void appendToGroup(ChunkGroup group) {
		if(!group.contains(this)) {
			group.add(this);
			for(Chunk chunk : this.links.keySet()) {
				chunk.appendToGroup(group);
			}
			if(this.group != null) {
				this.group.remove(this);
			}
			this.group = group;
			for(BaseEntity entity : this.entities) {
				EntityInternal.setWorld(entity, group.local);
			}
		}
	}

	public void tick() {
		var data = this.actions;
		int originals = 0;
		do {
			for(int i = data.size() - 1; i >= originals; i--) {
				if(this.execute(data, i)) {
					originals++;
				}
			}
		} while(this.actions.size() > originals);

		for(Iterator<BaseEntity> iterator = this.entities.iterator(); iterator.hasNext(); ) {
			BaseEntity entity = iterator.next();
			if(!EntityInternal.isHomeChunk(entity, this)) {
				iterator.remove();
			} else {
				EntityInternal.tick(entity);
			}
		}

		for(BaseEntity entity : this.entities) {
			EntityInternal.tickPos(entity); // allow entities to schedule chunk relocations
		}
	}

	/**
	 * @return if there are more tasks to execute
	 */
	public boolean runTasks() {
		for(int i = this.immediateTasks.size() - 1; i >= 0; i--) {
			Runnable runnable = this.immediateTasks.remove(i);
			runnable.run();
		}
		return !this.immediateTasks.isEmpty();
	}

	public long getId() {
		return combineInts(this.chunkX, this.chunkY);
	}

	public static long combineInts(int a, int b) {
		return (long) a << 32 | b & 0xFFFFFFFFL;
	}

	public static int getB(long id) {
		return (int) (id & 0xFFFFFFFFL);
	}

	public static int getA(long id) {
		return (int) ((id >>> 32) & 0xFFFFFFFFL);
	}

	public TileVariant get(TileLayers layer, int x, int y) {
		return this.variants[getIndex(layer, x, y)];
	}

	public TileData set(TileLayers layer, int x, int y, TileVariant value) {
		int index = getIndex(layer, x, y);
		TileVariant old = this.variants[index];
		TileData oldData = this.data.get(index);
		TileData replacement;
		// todo when attach api is added, add stack 'onReplace' with TileData so mods can choose on an individual basis
		//  whether or not
		//  their data is compatible with the new block
		if(value.isCompatible(oldData)) {
			replacement = oldData;
		} else {
			if(value.hasBlockData()) {
				replacement = value.createData();
				InternalTileDataAccess.init(replacement, index, this.chunkX, this.chunkY);
				this.data.put(index, replacement);
			} else {
				replacement = null;
				this.data.remove(index);
			}
		}

		this.variants[index] = value;

		// discard outdated actions
		for(int i = this.actions.size() - 1; i >= 0; i--) {
			UnpositionedTileData action = this.actions.get(i);
			if(!action.isCompatible(old, oldData, value, replacement)) {
				this.actions.remove(i);
			}
		}

		if(replacement != oldData && replacement != null) {
			if(value.doesTick(this.getWorld(), oldData, layer, ((InternalTileData)replacement).absX, ((InternalTileData)replacement).absY)) {
				this.actions.add(replacement);
			}
		}

		return replacement;
	}

	public World getWorld() {
		return this.group == null ? this.world : this.group.local;
	}

	public TileData getData(TileLayers layers, int x, int y) {
		return this.data.get(getIndex(layers, x, y));
	}

	public TileData setData(TileLayers layers, int x, int y, TileData data) {
		return this.data.put(getIndex(layers, x, y), data);
	}

	@Override
	public void execute(@NotNull Runnable command) {
		this.immediateTasks.add(command);
	}

	public ChunkGroup getGroup() {
		return this.group;
	}

	public void attachToGroup() {
		if(this.group == null) {
			this.world.requiresRelinking(this);
		}
	}

	static int getIndex(TileLayers layer, int x, int y) {
		return layer.ordinal() + TileLayers.COUNT * x + TileLayers.COUNT * World.CHUNK_SIZE * y;
	}

	private boolean execute(List<UnpositionedTileData> tileActions, int index) {
		UnpositionedTileData action = tileActions.get(index);

		TileData data = action instanceof TileData t ? t : null;
		TileVariant variant = data != null ? data.getVariant() : null;
		if(action instanceof TemporaryTileData.Tickable t) {
			variant = this.get(action.getLayer(), action.getLocalX(), action.getLocalY());
			if(variant.hasBlockData()) {
				data = this.getData(action.getLayer(), action.getLocalX(), action.getLocalY());
			}
			t.tick(this, this.group.local, variant, data, action.getLayer(),
				this.chunkX * World.CHUNK_SIZE + action.getLocalX(),
				this.chunkY * World.CHUNK_SIZE + action.getLocalY());
		}

		boolean shouldEnd = action.getCounter() <= 0 || (action.getCounter() != Integer.MAX_VALUE && action.getAndDecrement() <= 0);
		if(shouldEnd) {
			if(variant == null) {
				variant = this.get(action.getLayer(), action.getLocalX(), action.getLocalY());
				if(variant.hasBlockData()) {
					data = this.getData(action.getLayer(), action.getLocalX(), action.getLocalY());
				}
			}

			if(action instanceof TemporaryTileData t)
			t.onInvalidated(this, this.group.local, variant, data,
				action.getLayer(),
				this.chunkX * World.CHUNK_SIZE + action.getLocalX(),
				this.chunkY * World.CHUNK_SIZE + action.getLocalY());
			tileActions.remove(index);
			return false;
		}
		return true;
	}

	public int getChunkX() {
		return this.chunkX;
	}

	public int getChunkY() {
		return this.chunkY;
	}

	/**
	 * @return these entities may not be actually in the chunk, those are updated prior to entity tick
	 */
	public Iterable<BaseEntity> getRawEntities() {
		return this.filteredEntitiesView;
	}
}
