package rendering;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.client.WorldRenderer;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.jerraria.entity.PlayerEntity;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.SynchronousWorld;
import net.devtech.jerraria.world.internal.client.ClientWorld;
import net.devtech.jerraria.world.internal.client.ClientWorldServer;
import world.ChunkTests;

public class EntityRendering {
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			SynchronousWorld world = ChunkTests.setupServer(true);
			for(int i = 0; i < 256; i++) {
				for(int i1 = 0; i1 < 256; i1++) {
					world.blockLayer().putBlock(Tiles.DIRT.getDefaultVariant(), i, i1, 0);
				}
			}

			Entity player = new PlayerEntity(null);

			ClientWorldServer server = new ClientWorldServer();
			server.add(world);
			ClientWorld client = new ClientWorld(server, world);
			client.addEntity(player);

			RenderThread.addRenderStage(() -> {
				player.updatePosition(client, Math.sin(System.currentTimeMillis()/100d)*100, 0);
				Matrix3f mat = new Matrix3f();
				mat.offset(-1, 1);
				mat.scale(2, -2);
				mat.scale(JerrariaClient.windowHeight() / ((float) JerrariaClient.windowWidth()), 1);
				WorldRenderer renderer = new WorldRenderer(client);
				renderer.render(mat, player, 256, 256);
			}, 10);
			return null;
		});
	}
}
