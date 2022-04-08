package net.devtech.jerraria.world.tile.render;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;

import javax.annotation.Nullable;

public interface TileRenderer {
	// todo access api for TileRenderer
	// todo translucent rendering

	/**
	 * @param source source of shader objects <b>DO NOT RENDER DIRECTLY, USE SHADER OBJECTS FROM HERE!</b>
	 * @param tileMatrix the matrix that converts local tile coordinates to screen coordinates, so for example,
	 *                      [0, 0] would be in the top left corner of where your block should render,
	 *                      and [1, 1] at the bottom right.
	 *
	 * @param localWorld A snapshot of the area surrounding the block,
	 *                      generally you will have access to chunk data at-least 127 blocks in each direction,
	 *                      though this does not hold true for chunks on the edge of the client's render distance
	 *                      uses absolute tile coordinates.
	 * @param variant the current block
	 * @param clientTileData the clientside view of the tile data
	 * @param x absolute x of the tile in the world
	 * @param y absolute y of the tile in the world
	 */
	void renderTile(ShaderSource source, Matrix3f tileMatrix, World localWorld, TileVariant variant, @Nullable TileData clientTileData, int x, int y);
}
