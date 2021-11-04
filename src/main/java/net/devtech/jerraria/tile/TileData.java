package net.devtech.jerraria.tile;

/**
 * Any arbitrary data attached to a block
 */
public abstract class TileData implements VariantConvertable {
	TileVariant currentState;
	int absX, absY;

	@Override
	public TileVariant getVariant() {
		return this.currentState;
	}
}
