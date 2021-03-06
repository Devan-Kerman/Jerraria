package rendering;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.client.WorldRenderer;
import net.devtech.jerraria.client.render.world.OverworldWorldRenderer;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.jerraria.entity.PlayerEntity;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.SynchronousWorld;
import net.devtech.jerraria.world.internal.client.ClientWorld;
import net.devtech.jerraria.world.internal.client.ClientWorldServer;
import world.ChunkTests;

public class EntityRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			SynchronousWorld world = ChunkTests.setupServer(true);
			for(int x = 0; x < 256; x++) {
				for(int y = 0; y < 256; y++) {
					world.blockLayer().putBlock(Tiles.DIRT.getDefaultVariant(), x, y, 0);
				}
			}

			Entity player = new PlayerEntity(null);

			ClientWorldServer server = new ClientWorldServer();
			server.add(world);
			ClientWorld client = new ClientWorld(server, world);
			client.addEntity(player);

			WorldRenderer renderer = new OverworldWorldRenderer(client);
			RenderThread.addRenderStage(() -> {
				player.updatePosition(client, Math.sin(System.currentTimeMillis()/100d)*100, 0);
				Mat mat = Mat.create();
				mat.offset(-1, 1);
				mat.scale(2, -2);
				mat.scale(JerrariaClient.windowHeight() / ((float) JerrariaClient.windowWidth()), 1);
				renderer.render(mat, player, 256, 256);
			}, 10);
			return null;
		});
	}
}
