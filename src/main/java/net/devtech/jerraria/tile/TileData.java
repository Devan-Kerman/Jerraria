package net.devtech.jerraria.tile;

/**
 * Any arbitrary data attached to a block
 */
public abstract class TileData implements StateConvertable {
	private TileVariant currentState;
	private int absX, absY;

	@Override
	public TileVariant getState() {
		return this.currentState;
	}
}
