package net.devtech.jerraria.world.tile;

import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.InternalTileData;

/**
 * Any arbitrary data attached to a block
 */
public abstract class TileData extends InternalTileData implements VariantConvertable {
	TileVariant currentState;

	@Override
	public TileVariant getVariant() {
		return this.currentState;
	}
}
