package world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import net.devtech.jerraria.content.Tiles;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.chunk.Chunk;
import net.devtech.jerraria.world.internal.SynchronousWorld;
import net.devtech.jerraria.world.tile.TileVariant;
import org.junit.jupiter.api.Test;

public class WorldTest {
	@Test
	public void loadUnload() throws IOException {
		Path dir = Files.createTempDirectory("jerraria_test");
		Executor executor = Executors.newSingleThreadExecutor();
		SynchronousWorld world = new SynchronousWorld(dir, executor);
		Chunk chunk = world.getChunk(10, 10);
		TileVariant variant = Tiles.TEST.getDefaultVariant();
		chunk.ticket();
		chunk.set(TileLayers.BLOCK, 0, 0, variant);
		long start = System.currentTimeMillis();
		chunk.unticket();
		long endSer = System.currentTimeMillis();
		System.out.println("Serialization time(ms): " + (endSer - start));
		Chunk after = world.getChunk(10, 10);
		System.out.println("Deserialization time(ms): " + (System.currentTimeMillis() - endSer));
		System.out.println(after.get(TileLayers.BLOCK, 0, 0).getOwner());
	}
}
