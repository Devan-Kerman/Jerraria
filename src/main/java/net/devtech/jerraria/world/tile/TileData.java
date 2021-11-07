package net.devtech.jerraria.world.tile;

import net.devtech.jerraria.util.data.JCTagView;
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

	public abstract void tick(World world, TileLayers layer, int x, int y);
}
