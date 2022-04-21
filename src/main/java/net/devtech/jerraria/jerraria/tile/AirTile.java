package net.devtech.jerraria.jerraria.tile;

import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.TileRenderer;

public class AirTile extends Tile {
	static final TileRenderer NO_OP = (source, tileMatrix, localWorld, variant, clientTileData, x, y) -> {
	};

	public AirTile() {
	}

	@Override
	public TileRenderer getRenderer(TileVariant variant) {
		return NO_OP;
	}

}
