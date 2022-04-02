package world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.SynchronousWorld;
import net.devtech.jerraria.world.tile.TileVariant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChunkTests {
	public static SynchronousWorld setupServer(boolean maintainOrder) throws IOException {
		Path dir = Files.createTempDirectory("jerraria_test");
		System.out.println(dir.toAbsolutePath());
		Executor executor = ForkJoinPool.commonPool();
		return new SynchronousWorld(dir, executor, maintainOrder, null);
	}

	@Test
	public void loadUnload() throws IOException {
		SynchronousWorld world = setupServer(false);
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
		SynchronousWorld world = setupServer(true);
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
		SynchronousWorld world = setupServer(true);
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
		long start = System.currentTimeMillis();
		world.tick();
		long end = System.currentTimeMillis();
		System.out.println("World tick took " + (end - start) + "ms");
		// hope stack race condition happens, if it doesn't, idk try running it again it works on my machine
		Assertions.assertNotEquals(a, longs);
	}

	@Test
	public void syncTasks() throws IOException, InterruptedException {
		SynchronousWorld world = this.setupServer(true);
		boolean didNotRun = false;
		try {
			world.executeAt(0, 0).get(1, TimeUnit.SECONDS);
		} catch(InterruptedException | ExecutionException e) {
			throw Validate.rethrow(e);
		} catch(TimeoutException e) {
			didNotRun = true;
		}

		Assertions.assertTrue(didNotRun, "executor was run!");

		var ref = new Object() {
			int val;
		};
		world.executeAt(10, 10).thenAccept(group -> {
			ref.val = 1;
		});
		Assertions.assertEquals(0, ref.val, "task was run before tick!");
		world.tick();
		Assertions.assertEquals(1, ref.val, "task was not run!");
	}
}
