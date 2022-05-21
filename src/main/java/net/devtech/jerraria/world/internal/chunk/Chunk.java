package net.devtech.jerraria.world.internal.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.devtech.jerraria.jerracode.JCTagView;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.entity.EntityInternal;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.TickingWorld;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.VariantConvertable;
import org.jetbrains.annotations.NotNull;

public class Chunk implements Executor {
	/**
	 * how many ticks the chunk should wait before scheduling an unlink, this is to avoid relinking every-other tick
	 */
	public static final int UNLINKING_RELUCTANCE = 75;

	public final AbstractWorld world;
	public final int chunkX, chunkY;
	/**
	 * A flattened 3 dimensional array of each tile layer
	 */
	public final TileVariant[] variants = new TileVariant[World.CHUNK_SIZE * World.CHUNK_SIZE * TileLayers.COUNT];
	public final Int2ObjectMap<TileData> data;
	public final Set<Entity> entities;
	protected final List<UnpositionedTileData> actions;
	protected final List<Runnable> immediateTasks = new ArrayList<>();
	protected final Iterable<Entity> filteredEntitiesView;
	protected final Object2IntMap<Chunk> links;
	protected List<IntLongPair> unresolved;
	protected int ticketCount;
	protected int unlinkTimer;
	protected ChunkGroup blockGroup;

	public Chunk(AbstractWorld world, int chunkX, int chunkY) {
		Arrays.fill(this.variants, Tiles.AIR.getDefaultVariant());
		this.data = new Int2ObjectOpenHashMap<>();
		this.actions = new ArrayList<>();
		this.links = world.doesMaintainOrder() ? new Object2IntLinkedOpenHashMap<>() : new Object2IntOpenHashMap<>();
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.entities = new HashSet<>();
		//noinspection StaticPseudoFunctionalStyleMethod
		this.filteredEntitiesView = Iterables.filter(this.entities, Entity::inWorld);
	}

	public Chunk(AbstractWorld world, int chunkX, int chunkY, JCTagView tag) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		ChunkCodec.populateTiles(this.variants, tag.get("tiles", NativeJCType.POOLED_TAG_LIST));
		this.data = ChunkCodec.deserializeData(chunkX,
			chunkY,
			this.variants,
			tag.get("data", NativeJCType.INT_ANY_LIST)
		);
		this.unresolved = tag.get("links", NativeJCType.INT_LONG_LIST);
		this.links = new Object2IntOpenHashMap<>(this.unresolved.size());
		this.actions = ChunkCodec.deserializeTemporaryData(this, tag.get("actions", NativeJCType.ID_ANY_LIST));
		this.entities = ChunkCodec.deserializeEntities(world, tag.get("entities", NativeJCType.ENTITIES));
		//noinspection StaticPseudoFunctionalStyleMethod
		this.filteredEntitiesView = Iterables.filter(this.entities, Entity::inWorld);
		for(Entity entity : this.entities) {
			EntityInternal.setHomeChunk(entity, this);
		}
	}

	public void addEntity(Entity entity) {
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

	public <T extends UnpositionedTileData> T schedule(
		TemporaryTileData.Type<T> type,
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
		if(this.blockGroup != null) {
			this.blockGroup.ticket();
		}
	}

	public void unticket() {
		this.ticketCount--;
		if(this.blockGroup != null) {
			this.blockGroup.unticket();
		} else if(this.ticketCount == 0) {
			((TickingWorld) this.world).unloadIndividualChunk(this);
		}
	}


	public void removeLink(Chunk chunk) {
		if(chunk == this) {
			return;
		}
		synchronized(chunk.links) {
			chunk.links.computeIntIfPresent(this, (c, i) -> i - 1);
		}
		synchronized(this) {
			int links = this.links.computeIntIfPresent(chunk, (c, i) -> i - 1);
			if(links <= 0) {
				this.unlinkTimer = 1;
			}
		}
	}

	public void addLink(Chunk chunk) {
		if(chunk == this) {
			return;
		}
		synchronized(chunk.links) {
			chunk.links.mergeInt(this, 1, (c, i) -> i + 1);
		}
		synchronized(this) {
			int links = this.links.mergeInt(chunk, 1, (c, i) -> i + 1);
			this.unlinkTimer = 0;
			if(links == 1) {
				((TickingWorld) this.world).requiresRelinking(this);
			}
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
			if(this.blockGroup != null) {
				this.blockGroup.remove(this);
			}
			this.blockGroup = group;
			for(Entity entity : this.entities) {
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

		for(Entity entity : this.entities) {
			EntityInternal.tick(entity);
		}

		if(this.unlinkTimer >= 1 && ++this.unlinkTimer >= UNLINKING_RELUCTANCE) {
			((TickingWorld) this.world).requiresRelinking(this);
			this.blockGroup = null;
		}
		// todo remove when ticketless
	}

	public void moveEntities() {
		Iterator<Entity> entities = this.entities.iterator();
		while(entities.hasNext()) {
			Entity entity = entities.next();
			EntityInternal.tickPos(entity);
			if(!EntityInternal.isHomeChunk(entity, this)) {
				entities.remove();
			}
		}
	}

	/**
	 * @return if there are more tasks to execute
	 */
	public boolean runTasks() {
		boolean run = false;
		for(int i = this.immediateTasks.size() - 1; i >= 0; i--) {
			Runnable runnable = this.immediateTasks.remove(i);
			runnable.run();
			run = true;
		}
		return run;
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

	// todo get & set
	// set & get (current + success/failure context)
	public enum ReturnType {
		NEWLY_CREATED,
		REPLACED
	}

	public VariantConvertable set(TileLayers layer, int x, int y, TileVariant value, int flags, boolean newlyCreated) {
		int index = getIndex(layer, x, y);
		TileVariant old = this.variants[index];
		TileData oldData = this.data.get(index);
		TileData replacement;
		// todo when attach api is added, add stack 'onReplace' with TileData so mods can choose on an individual basis
		//  whether or not
		//  their data is compatible with the new block
		if(value.isCompatible(oldData, old)) {
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
			if(value.doesTick(
				this.getWorld(),
				oldData,
				layer,
				((InternalTileData) replacement).absX,
				((InternalTileData) replacement).absY
			)) {
				this.actions.add(replacement);
			}
		}

		if(newlyCreated) {
			return replacement == null ? value : replacement;
		} else {
			return oldData == null ? old : oldData;
		}
	}

	public World getWorld() {
		return this.blockGroup == null ? this.world : this.blockGroup.local;
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

	public ChunkGroup getBlockGroup() {
		return this.blockGroup;
	}

	public void attachToGroup() {
		if(this.blockGroup == null) {
			((TickingWorld) this.world).requiresRelinking(this);
		}
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
	public Iterable<Entity> getRawEntities() {
		return this.entities;
	}

	public Iterable<Entity> getEntities() {
		return this.filteredEntitiesView;
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
			if(variant.hasBlockData() && data == null) {
				data = this.getData(action.getLayer(), action.getLocalX(), action.getLocalY());
			}
			t.tick(this,
				this.blockGroup.local,
				variant,
				data,
				action.getLayer(),
				this.chunkX * World.CHUNK_SIZE + action.getLocalX(),
				this.chunkY * World.CHUNK_SIZE + action.getLocalY()
			);
		}

		boolean shouldEnd =
			action.getCounter() <= 0 || (action.getCounter() != Integer.MAX_VALUE && action.getAndDecrement() <= 0);
		if(shouldEnd) {
			if(variant == null) {
				variant = this.get(action.getLayer(), action.getLocalX(), action.getLocalY());
				if(variant.hasBlockData()) {
					data = this.getData(action.getLayer(), action.getLocalX(), action.getLocalY());
				}
			}

			if(action instanceof TemporaryTileData t) {
				t.onInvalidated(
					this,
					this.blockGroup.local,
					variant,
					data,
					action.getLayer(),
					this.chunkX * World.CHUNK_SIZE + action.getLocalX(),
					this.chunkY * World.CHUNK_SIZE + action.getLocalY()
				);
			}
			tileActions.remove(index);
			return false;
		}
		return true;
	}
}
