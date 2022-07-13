package rendering;

import net.devtech.jerraria.client.render.world.OverworldWorldRenderer;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.jerraria.entity.PlayerEntity;
import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.client.ClientChunk;
import net.devtech.jerraria.world.internal.client.ClientWorld;
import net.devtech.jerraria.world.internal.client.ClientWorldServer;
import org.lwjgl.glfw.GLFW;

public class ClientWorldRendering {
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			ClientWorldServer server = new ClientWorldServer();
			ClientWorld world = new ClientWorld(server, 0);
			server.add(world);

			ClientChunk value = new ClientChunk(world, 0, 0);
			world.loadedChunkCache.put(0, value);

			ClientChunk test = new ClientChunk(world, 0, -1);
			System.out.println(test);
			world.loadedChunkCache.put(JMath.combineInts(0, -1), test);
			test.setDelayUpdates();
			for (int x = 0; x < 256; x++) {
				for (int y = 0; y < 256; y++) {
					test.set(TileLayers.BLOCK, x, y, Tiles.DIRT.getDefaultVariant(), 0, false);
				}
			}
			test.flushUpdates();

			value.set(TileLayers.BLOCK, 1, 4, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 2, 4, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 3, 4, Tiles.DIRT.getDefaultVariant(), 0, false);

			value.set(TileLayers.BLOCK, 0, 3, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 1, 3, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 2, 3, Tiles.GRASS.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 3, 3, Tiles.GRASS.getDefaultVariant(), 0, false);

			value.set(TileLayers.BLOCK, 0, 2, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 1, 2, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 2, 2, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 3, 2, Tiles.DIRT.getDefaultVariant(), 0, false);

			value.set(TileLayers.BLOCK, 0, 1, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 1, 1, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 2, 1, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 3, 1, Tiles.DIRT.getDefaultVariant(), 0, false);

			value.set(TileLayers.BLOCK, 1, 0, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 3, 0, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 10, 0, Tiles.DIRT.getDefaultVariant(), 0, false);
			value.set(TileLayers.BLOCK, 11, 0, Tiles.DIRT.getDefaultVariant(), 0, false);

			Entity player = new PlayerEntity(null);

			int[] scale = {256};
			GLFW.glfwSetKeyCallback(JerrariaClient.MAIN_WINDOW_GL_ID, (window, key, scancode, action, mods) -> {
				if(action == GLFW.GLFW_PRESS) {
					switch (key) {
						case GLFW.GLFW_KEY_LEFT -> player.updatePosition(world, player.x() - 1, player.y());
						case GLFW.GLFW_KEY_RIGHT -> player.updatePosition(world, player.x() + 1, player.y());
						case GLFW.GLFW_KEY_UP -> player.updatePosition(world, player.x(), player.y() + 1);
						case GLFW.GLFW_KEY_DOWN -> player.updatePosition(world, player.x(), player.y() - 1);
						case GLFW.GLFW_KEY_EQUAL -> scale[0] *= 2;
						case GLFW.GLFW_KEY_MINUS -> scale[0] = Math.max(scale[0] / 2, 2);
					}
					System.out.println(player.getBlockX() + " " + player.getBlockY());
				}
			});

			WorldRenderer renderer = new OverworldWorldRenderer(world);
			RenderThread.addRenderStage(() -> {
				Mat mat = Mat.create();
				mat.offset(-1, 1);
				mat.scale(2, -2);
				mat.scale(JerrariaClient.windowHeight() / ((float) JerrariaClient.windowWidth()), 1);
				renderer.render(mat, player, scale[0], scale[0]);
			}, 10);
			return null;
		});
	}
}
