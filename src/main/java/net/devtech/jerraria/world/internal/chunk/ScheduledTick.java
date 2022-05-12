package net.devtech.jerraria.world.internal.chunk;

import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import org.jetbrains.annotations.Nullable;

public class ScheduledTick extends TemporaryTileData {
	public static final TemporaryTileData.Type<ScheduledTick> TYPE = UnpositionedTileData.createAndRegister(ScheduledTick::new, ScheduledTick::new, ScheduledTick::write,
		Id.createFull("jerraria", "scheduled"));

	protected ScheduledTick(Type<?> type, TileLayers layer, int localX, int localY, int time) {
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
		variant.getOwner().onScheduledTick(world, variant, data, layers, x, y);
	}

	protected ScheduledTick(Type<?> type, JCElement<Long> packedData) {
		super(type, packedData.value());
	}


	private JCElement<Long> write(Type<?> type) {
		return JCElement.create(NativeJCType.LONG, this.encode());
	}

	@Override
	public boolean isCompatible(TileVariant old, TileData oldData, TileVariant new_, TileData newData) {
		return old.getOwner() == new_.getOwner();
	}
}
