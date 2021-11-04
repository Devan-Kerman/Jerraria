package net.devtech.jerraria.world.chunk;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;

public class Chunk {
	final int chunkX, chunkY;
	/**
	 * A flattened 3 dimensional array of each tile layer
	 */
	final TileVariant[] variants = new TileVariant[World.CHUNK_SIZE * World.CHUNK_SIZE * TileLayers.COUNT];
	final Int2ObjectMap<TileData> data = new Int2ObjectOpenHashMap<>();
	final ArrayList<ScheduledTileAction> actions = new ArrayList<>();

	public Chunk(int chunkX, int chunkY) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
	}

	public void tick(World world) {
		for(TileData value : data.values()) {
			value.tick();
		}

		int originals = this.actions.size();
		List<ScheduledTileAction> tileActions = this.actions;
		for(int i = tileActions.size() - 1; i >= 0; i--) {
			ScheduledTileAction action = tileActions.get(i);
			if(action.counter == action.delay) {
				this.runAction(world, action);
				action.counter = 0;
				tileActions.remove(i);
				originals--;
			}
		}

		while(this.actions.size() > originals) {
			for(int i = tileActions.size() - 1; i >= originals; i--) {
				ScheduledTileAction action = tileActions.get(i);
				if(action.delay == 0) {
					this.runAction(world, action);
					action.counter = 0;
					tileActions.remove(i);
					originals--;
				}
			}
			originals = this.actions.size();
		}
	}

	private void runAction(World world, ScheduledTileAction action) {
		TileVariant variant = this.get(action.layer, action.localX, action.localY);
		TileData data = this.getData(action.layer, action.localX, action.localY);
		action.run(variant, data, world, this.chunkX * World.CHUNK_SIZE + action.localX, this.chunkY * World.CHUNK_SIZE + action.localY);
	}

	public TileVariant get(TileLayers layer, int x, int y) {
		return this.variants[this.getIndex(layer, x, y)];
	}

	public TileVariant set(TileLayers layer, int x, int y, TileVariant value) {
		int index = this.getIndex(layer, x, y);
		TileVariant old = this.variants[index];
		this.variants[index] = value;

		// discard outdated actions
		for(int i = this.actions.size() - 1; i >= 0; i--) {
			ScheduledTileAction action = this.actions.get(i);
			if(action.isIncompatible(old, value)) {
				this.actions.remove(i);
			}
		}

		return old;
	}

	public TileData getData(TileLayers layers, int x, int y) {
		return this.data.get(this.getIndex(layers, x, y));
	}

	public TileData setData(TileLayers layers, int x, int y, TileData data) {
		return this.data.put(this.getIndex(layers, x, y), data);
	}

	private int getIndex(TileLayers layer, int x, int y) {
		return layer.ordinal() + TileLayers.COUNT * x + TileLayers.COUNT * World.CHUNK_SIZE * y;
	}
}
