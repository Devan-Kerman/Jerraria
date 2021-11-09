package world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.devtech.jerraria.content.Tiles;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.chunk.Chunk;
import net.devtech.jerraria.world.internal.SynchronousWorld;
import net.devtech.jerraria.world.tile.TileVariant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorldTest {
	@Test
	public void loadUnload() throws IOException {
		Path dir = Files.createTempDirectory("jerraria_test");
		Executor executor = Executors.newSingleThreadExecutor();
		SynchronousWorld world = new SynchronousWorld(dir, executor, false);
		Chunk chunk = world.getChunk(10, 10);
		TileVariant variant = Tiles.TEST.getDefaultVariant();
		chunk.ticket();
		chunk.set(TileLayers.BLOCK, 0, 0, variant);
		chunk.unticket();
		Chunk after = world.getChunk(10, 10);
		Assertions.assertEquals(variant, after.get(TileLayers.BLOCK, 0, 0));
	}

	@Test
	public void synchronizedTicking() throws IOException {
		Path dir = Files.createTempDirectory("jerraria_test");
		Executor executor = ForkJoinPool.commonPool();
		SynchronousWorld world = new SynchronousWorld(dir, executor, true);
		Chunk test = world.getChunk(-1, -1);
		Vector<Long> longs = new Vector<>();
		LongList a = new LongArrayList();
		for(int cx = 0; cx < 10; cx++) {
			for(int cy = 0; cy < 10; cy++) {
				Chunk chunk = world.getChunk(cx, cy);
				TestTemporaryData data = chunk.schedule(TestTemporaryData.TYPE, TileLayers.BLOCK, 0, 0, 0);
				a.add(chunk.getId());
				data.ids = longs;
				chunk.addLink(test);
			}
		}
		world.tick();
		Assertions.assertEquals(a, longs);
	}

	@Test
	public void concurrentTicking() throws IOException {
		Path dir = Files.createTempDirectory("jerraria_test");
		Executor executor = ForkJoinPool.commonPool();
		SynchronousWorld world = new SynchronousWorld(dir, executor, true);
		Vector<Long> longs = new Vector<>();
		LongList a = new LongArrayList();
		for(int cx = 0; cx < 10; cx++) {
			for(int cy = 0; cy < 10; cy++) {
				Chunk chunk = world.getChunk(cx, cy);
				TestTemporaryData data = chunk.schedule(TestTemporaryData.TYPE, TileLayers.BLOCK, 0, 0, 0);
				a.add(chunk.getId());
				data.ids = longs;
			}
		}
		world.tick();
		// hope a race condition happens, if it doesn't, idk try running it again it works on my machine
		Assertions.assertNotEquals(a, longs);
	}
}
