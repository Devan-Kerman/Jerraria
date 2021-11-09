package net.devtech.jerraria.world.chunk;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.data.JCElement;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.Nullable;

public class TempLink extends TemporaryTileData<LongList> {
	public static final Type<LongList> TYPE = TemporaryTileData.createAndRegister(
		TempLink::new,
		TempLink::new,
		Id.createFull("jerraria", "tempcklink"));

	LongList unresolved;
	List<Chunk> links;

	protected TempLink(Type<LongList> type, TileLayers layer, int localX, int localY, int delay) {
		super(type, layer, localX, localY, delay);
	}

	protected TempLink(Type<LongList> type, JCElement<LongList> data) {
		super(type, data.value().getLong(0));
		LongList value = data.value();
		this.unresolved = value.subList(1, value.size());
	}

	public void link(int cx, int cy) {
		if(this.unresolved == null) {
			this.unresolved = new LongArrayList();
		}
		this.unresolved.add(Chunk.combineInts(cx, cy));
	}

	public void link(Chunk chunk) {
		if(this.links == null) {
			this.links = new ArrayList<>();
		}
		this.links.add(chunk);
	}

	@Override
	protected void onInvalidated(Chunk chunk, TileVariant variant, @Nullable TileData data, World world, int x, int y) {
		for(Chunk link : this.resolve(world)) {
			chunk.removeLink(link);
		}
	}

	@Override
	protected boolean isCompatible(TileVariant old, TileVariant new_) {
		return false; // todo
	}

	@Override
	protected JCElement<LongList> write() {
		LongList longs = new LongArrayList();
		longs.add(this.encode());
		for(Chunk link : this.links) {
			longs.add(link.getId());
		}
		return new JCElement<>(NativeJCType.LONG_ARRAY, longs);
	}

	protected List<Chunk> resolve(World world) {
		if(this.links == null) {
			this.links = new ArrayList<>();
		}

		if(this.unresolved != null) {
			for(long a : this.unresolved) {
				this.links.add(world.getChunk(Chunk.getA(a), Chunk.getB(a)));
			}
			this.unresolved = null;
		}
		return this.links;
	}
}
