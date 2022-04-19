package net.devtech.jerraria.client;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.client.ClientChunk;
import net.devtech.jerraria.world.internal.client.ClientWorld;

public class WorldRenderer {
	final ClientWorld world;

	public WorldRenderer(ClientWorld world) {
		this.world = world;
	}

	public void render(Matrix3f cartToAwt, Entity player, int blockScreenWidth, int blockScreenHeight) {
		Matrix3f rel = cartToAwt.copy().scale(1f/blockScreenWidth, 1f/blockScreenHeight);

		int extendedOffX = blockScreenWidth / 2 + 10, extendedOffY = blockScreenHeight / 2 + 10;
		int blockX = player.getBlockX(), blockY = player.getBlockY();

		int fromBlockX = blockX - extendedOffX, fromBlockY = blockY - extendedOffY;
		int toBlockX   = blockX + extendedOffX,   toBlockY = blockY + extendedOffY;

		// block coordinate of top left corner
		double fromBlockXScreen = player.getX() - blockScreenWidth / 2f, fromBlockYScreen = player.getY() - blockScreenHeight / 2f;

		// y coordinates might be inverted idfk, this is literal hell
		for(int cx = (fromBlockX >> World.LOG2_CHUNK_SIZE); cx <= (toBlockX >> World.LOG2_CHUNK_SIZE); cx++) {
			for(int cy = (fromBlockY >> World.LOG2_CHUNK_SIZE); cy <= (toBlockY >> World.LOG2_CHUNK_SIZE); cy++) {
				int bx = cx * World.CHUNK_SIZE, by = cy * World.CHUNK_SIZE;
				float offX = (float) (bx - fromBlockXScreen), offY = (float) (by - fromBlockYScreen);
				Matrix3f chunkMatrix = rel.copy().offset(offX, offY);
				ClientChunk chunk = (ClientChunk) this.world.getChunk(cx, cy);
				if(chunk != null) {
					chunk.render(chunkMatrix);
				}
			}
		}
	}
}
