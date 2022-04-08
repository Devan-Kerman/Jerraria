package net.devtech.jerraria.render.world;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.World;

public class WorldRenderer {
	final World world;

	public WorldRenderer(World world) {
		this.world = world;
	}

	public void renderWorld(Matrix3f matrix, double playerX, double playerY, int spanX, int spanY) {
		int sx = (int) (playerX - spanX/2), sy = (int) (playerY - spanY/2), ex = (int) (playerX + spanX/2), ey = (int) (playerX + spanX / 2);
		for(int cx = (sx >> World.LOG2_CHUNK_SIZE); cx <= (ex >> World.LOG2_CHUNK_SIZE); cx++) {
			for(int cy = (sy >> World.LOG2_CHUNK_SIZE); cy <= (ey >> World.LOG2_CHUNK_SIZE); cy++) {
				// render chunk

			}
		}
	}
}
