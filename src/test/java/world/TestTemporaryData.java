package world;

import java.util.Vector;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.chunk.TemporaryTileData;
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

	@Override
	protected void onInvalidated(Chunk chunk,
		World world,
		TileVariant variant,
		@Nullable TileData data,
		TileLayers layers,
		int x,
		int y) {
		this.ids.add(chunk.getId());
	}

	public TestTemporaryData(Type<?> type, JCElement<Long> packedData) {
		super(type, packedData.value());
	}


	@Override
	public boolean isCompatible(TileVariant old, TileData oldData, TileVariant new_, TileData newData) {
		return false;
	}

	protected JCElement<Long> write(Type<?> type) {
		return JCElement.create(NativeJCType.LONG, this.encode());
	}
}
