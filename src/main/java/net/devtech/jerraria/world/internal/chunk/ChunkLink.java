package net.devtech.jerraria.world.internal.chunk;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.Nullable;

public class ChunkLink extends TemporaryTileData {
	public static final Type<ChunkLink> TYPE = UnpositionedTileData.createAndRegister(ChunkLink::new,
		ChunkLink::new, ChunkLink::write,
		Id.createFull("jerraria", "tempcklink"));

	LongList unresolved;
	List<Chunk> links;

	protected ChunkLink(Type<? extends ChunkLink> type, TileLayers layer, int localX, int localY, int delay) {
		super(type, layer, localX, localY, delay);
	}

	protected ChunkLink(Type<? extends ChunkLink> type, JCElement<LongList> data) {
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

	public JCElement<LongList> write(Type<? > type) {
		LongList longs = new LongArrayList();
		longs.add(this.encode());
		for(Chunk link : this.links) {
			longs.add(link.getId());
		}
		return JCElement.create(NativeJCType.LONG_ARRAY, longs);
	}


	@Override
	protected void onInvalidated(Chunk chunk,
		World world,
		TileVariant variant,
		@Nullable TileData data,
		TileLayers layers,
		int x,
		int y) {
		for(Chunk link : this.resolve(world)) {
			chunk.removeLink(link);
		}
	}

	@Override
	public boolean isCompatible(TileVariant old, TileData oldData, TileVariant new_, TileData newData) {
		return false; // todo
	}

	protected List<Chunk> resolve(World world) {
		if(this.links == null) {
			this.links = new ArrayList<>();
		}

		if(this.unresolved != null) {
			for(long a : this.unresolved) {
				this.links.add(((AbstractWorld) world).getChunk(Chunk.getA(a), Chunk.getB(a)));
			}
			this.unresolved = null;
		}
		return this.links;
	}
}
