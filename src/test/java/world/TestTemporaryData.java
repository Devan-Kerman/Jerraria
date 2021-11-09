package world;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.chunk.Chunk;
import net.devtech.jerraria.world.chunk.TemporaryTileData;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.Nullable;

public class TestTemporaryData extends TemporaryTileData {
	public static final TemporaryTileData.Type<TestTemporaryData> TYPE = createAndRegister(TestTemporaryData::new,
		TestTemporaryData::new,
		TestTemporaryData::write,
		Id.createFull("jerraria", "test_data"));
	public Vector<Long> ids;

	public TestTemporaryData(Type<?> type, TileLayers layer, int localX, int localY, int time) {
		super(type, layer, localX, localY, time);
	}

	public TestTemporaryData(Type<?> type, JCElement<Long> packedData) {
		super(type, packedData.value());
	}

	@Override
	protected void onInvalidated(Chunk chunk, TileVariant variant, @Nullable TileData data, World world, int x, int y) {
		this.ids.add(chunk.getId());
	}

	@Override
	protected boolean isCompatible(TileVariant old, TileVariant new_) {
		return false;
	}

	protected JCElement<Long> write(Type<?> type) {
		return JCElement.newInstance(NativeJCType.LONG, this.encode());
	}
}
