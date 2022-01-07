package net.devtech.jerraria.tile;

import net.devtech.jerraria.world.internal.chunk.InternalTileData;

/**
 * Any arbitrary data attached to stack block
 */
public abstract class TileData extends InternalTileData implements VariantConvertable {
	TileVariant currentState;

	@Override
	public TileVariant getVariant() {
		return this.currentState;
	}
}
