package net.devtech.jerraria.client;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.entity.render.EntityRenderer;
import net.devtech.jerraria.world.internal.client.ClientChunk;
import net.devtech.jerraria.world.internal.client.ClientWorld;

public abstract class WorldRenderer {
	final ClientWorld world;

	public WorldRenderer(ClientWorld world) {
		this.world = world;
	}

	protected abstract void renderBackground(Matrix3f cartToAwt, Entity player, int windowFromX, int windowFromY, int windowToX, int windowToY);

	public void render(Matrix3f cartToAwt, Entity player, int blockScreenWidth, int blockScreenHeight) {
		Matrix3f rel = cartToAwt.copy().scale(1f/blockScreenWidth, 1f/blockScreenHeight);

		int extendedOffX = blockScreenWidth / 2 + 10, extendedOffY = blockScreenHeight / 2 + 10;
		int blockX = player.getBlockX(), blockY = player.getBlockY();

		int fromBlockX = blockX - extendedOffX, fromBlockY = blockY - extendedOffY;
		int toBlockX   = blockX + extendedOffX,   toBlockY = blockY + extendedOffY;

		this.renderBackground(cartToAwt, player, fromBlockX, fromBlockY, toBlockX, toBlockY);
		Matrix3f chunkMatrix = new Matrix3f();
		// block coordinate of top left corner
		double fromBlockXScreen = player.x() - blockScreenWidth / 2f, fromBlockYScreen = player.y() + blockScreenHeight / 2f;
		for(int cx = (fromBlockX >> World.LOG2_CHUNK_SIZE); cx <= (toBlockX >> World.LOG2_CHUNK_SIZE); cx++) {
			for(int cy = (fromBlockY >> World.LOG2_CHUNK_SIZE); cy <= (toBlockY >> World.LOG2_CHUNK_SIZE); cy++) {
				int bx = cx * World.CHUNK_SIZE, topLeftY = World.CHUNK_SIZE + cy * World.CHUNK_SIZE;
				float offX = (float) (bx - fromBlockXScreen), offY = (float) (fromBlockYScreen - topLeftY);
				chunkMatrix.load(rel).offset(offX, offY);
				ClientChunk chunk = (ClientChunk) this.world.getChunk(cx, cy);
				if(chunk != null) {
					chunk.render(chunkMatrix);
				}
			}
		}

		Matrix3f entityMatrix = new Matrix3f();
		// multithreaded entity rendering could be a possibility?
		// todo batch entities
		//this.world.entityLayer().getEntitiesIntersect(EntitySearchType.Standard.RENDERING, fromBlockX, fromBlockY, toBlockX, toBlockY, 10).forEach(entity -> {
		//	EntityRenderer renderer = entity.getRenderer();
		//	float offX = (float) (entity.x() - fromBlockXScreen), offY = (float) (fromBlockYScreen - entity.y());
		//	entityMatrix.load(rel).offset(offX, offY);
		//	renderer.renderEntity(entity, entityMatrix, fromBlockX, fromBlockY, toBlockX, toBlockY);
		//	EntityRenderer extra = EntityRenderer.EXTRA_ENTITY_RENDERER.get().apply(entity);
		//	if(extra != null) {
		//		extra.renderEntity(entity, entityMatrix, fromBlockX, fromBlockY, toBlockX, toBlockY);
		//	}
		//});
	}
}
