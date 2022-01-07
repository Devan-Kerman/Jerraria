package net.devtech.jerraria.tile;

import net.devtech.jerraria.util.data.element.JCElement;
import org.jetbrains.annotations.Nullable;

public abstract class TileWithData extends Tile {
	@Override
	protected abstract TileData create(TileVariant variant);

	@Override
	protected abstract JCElement<?> write(TileData data, TileVariant variant);

	@Override
	protected abstract @Nullable TileData read(TileVariant variant, JCElement<?> view);
}
