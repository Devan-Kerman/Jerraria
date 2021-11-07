package net.devtech.jerraria.world.tile;

import net.devtech.jerraria.util.data.JCTagView;
import org.jetbrains.annotations.Nullable;

public abstract class TileWithData extends Tile {
	@Override
	protected abstract TileData create(TileVariant variant);

	@Override
	protected abstract void write(TileData data, TileVariant variant, JCTagView.Builder builder);

	@Override
	protected abstract @Nullable TileData read(TileVariant variant, JCTagView view);
}
