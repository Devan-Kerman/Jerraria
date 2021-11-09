package net.devtech.jerraria.world.chunk;

import static net.devtech.jerraria.world.tile.InternalTileAccess.getAbsX;
import static net.devtech.jerraria.world.tile.InternalTileAccess.getAbsY;
import static net.devtech.jerraria.world.tile.InternalTileAccess.getLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import net.devtech.jerraria.world.internal.ChunkCodec;
import net.devtech.jerraria.world.internal.TickingWorld;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;

public class Chunk {
	final TickingWorld world;
	final int chunkX, chunkY;
	/**
	 * A flattened 3 dimensional array of each tile layer
	 */
	final TileVariant[] variants = new TileVariant[World.CHUNK_SIZE * World.CHUNK_SIZE * TileLayers.COUNT];
	final Int2ObjectMap<TileData> data;
	final List<TemporaryTileData> actions;
	final Object2IntMap<Chunk> links;
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
	}

	public Chunk(TickingWorld world, int chunkX, int chunkY, JCTagView tag) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		ChunkCodec.populateTiles(variants, tag.get("tiles", NativeJCType.POOLED_TAG_LIST));
		this.data = ChunkCodec.deserializeData(
			world,
			chunkX,
			chunkY,
			this.variants,
			tag.get("data", NativeJCType.INT_ANY_LIST));
		this.actions = ChunkCodec.deserializeTemporaryData(tag.get("actions", NativeJCType.ID_ANY_LIST));
		this.unresolved = tag.get("links", NativeJCType.INT_LONG_LIST);
		this.links = new Object2IntOpenHashMap<>(unresolved.size());
	}

	public JCTagView write() {
		JCTagView.Builder tag = JCTagView.builder();
		tag.put("tiles", NativeJCType.POOLED_TAG_LIST, ChunkCodec.writeTiles(this.variants));
		tag.put("data", NativeJCType.INT_ANY_LIST, ChunkCodec.serializeData(this.variants, this.data));
		tag.put("actions", NativeJCType.ID_ANY_LIST, ChunkCodec.serializeTemporaryData(this.actions));
		tag.put("links", NativeJCType.INT_LONG_LIST, ChunkCodec.serializeLinks(this.links));
		return tag;
	}


	public <T extends TemporaryTileData> T schedule(TemporaryTileData.Type<T> type, TileLayers layer, int x, int y, int duration) {
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
		}
	}

	// todo less memory intensive linking system?

	public void removeLink(Chunk chunk) {
		chunk.links.computeIntIfPresent(this, (c, i) -> i - 1);
		if(this.links.computeIntIfPresent(chunk, (c, i) -> i - 1) <= 0) {
			this.world.requiresRelinking(this);
			this.group = null;
		}
	}

	public void addLink(Chunk chunk) {
		chunk.links.mergeInt(this, 1, (c, i) -> i + 1);
		if(this.links.mergeInt(chunk, 1, (c, i) -> i + 1) == 1) {
			this.world.requiresRelinking(this);
		}
	}

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
		}
	}


	public void tick() {
		for(TileData value : this.data.values()) {
			value.tick(this.world, getLayer(value), getAbsX(value), getAbsY(value));
		}

		List<TemporaryTileData> data = this.actions;
		int originals = 0;
		do {
			for(int i = data.size() - 1; i >= originals; i--) {
				if(this.execute(data, i)) {
					originals++;
				}
			}
		} while(this.actions.size() > originals);
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
		TileData data = this.data.get(index);
		TileData replacement;
		// todo when attach api is added, add a 'onReplace' with TileData so mods can choose on an individual basis
		//  whether or not
		//  their data is compatible with the new block
		if(value.isCompatible(data)) {
			if(value.hasBlockData()) {
				replacement = value.createData();
				this.data.put(index, replacement);
			} else {
				replacement = null;
				this.data.remove(index);
			}
		} else {
			replacement = data;
		}

		this.variants[index] = value;

		// discard outdated actions
		for(int i = this.actions.size() - 1; i >= 0; i--) {
			TemporaryTileData action = this.actions.get(i);
			if(!action.isCompatible(old, value)) {
				this.actions.remove(i);
			}
		}

		return replacement;
	}

	public TileData getData(TileLayers layers, int x, int y) {
		return this.data.get(getIndex(layers, x, y));
	}

	public TileData setData(TileLayers layers, int x, int y, TileData data) {
		return this.data.put(getIndex(layers, x, y), data);
	}

	private static int getIndex(TileLayers layer, int x, int y) {
		return layer.ordinal() + TileLayers.COUNT * x + TileLayers.COUNT * World.CHUNK_SIZE * y;
	}

	private boolean execute(List<TemporaryTileData> tileActions, int index) {
		TemporaryTileData action = tileActions.get(index);
		if(action.counter <= 0 || (action.counter != Integer.MAX_VALUE && --action.counter == 0)) {
			this.runAction(this.world, action);
			tileActions.remove(index);
			return false;
		}
		return true;
	}

	private void runAction(World world, TemporaryTileData action) {
		TileVariant variant = this.get(action.layer, action.localX, action.localY);
		TileData data = this.getData(action.layer, action.localX, action.localY);
		action.onInvalidated(
			this,
			variant,
			data,
			world,
			this.chunkX * World.CHUNK_SIZE + action.localX,
			this.chunkY * World.CHUNK_SIZE + action.localY);
	}
}
