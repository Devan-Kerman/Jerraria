package net.devtech.jerraria.tile;

import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;

/**
 * Any arbitrary data attached to a block
 */
public abstract class TileData implements VariantConvertable {
	TileVariant currentState;
	TileLayers layer;
	World world;
	int absX, absY;



	@Override
	public TileVariant getVariant() {
		return this.currentState;
	}

	public abstract void tick();
}
