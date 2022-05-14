package net.devtech.jerraria.client;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.JCommander;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.jerraria.entity.PlayerEntity;
import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.PathVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.client.ClientChunk;
import net.devtech.jerraria.world.internal.client.ClientWorld;
import net.devtech.jerraria.world.internal.client.ClientWorldServer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ClientMain {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static OverlayDirectory clientResources;
	public static void main(String[] argv) {
		ClientArgs args = new ClientArgs();
		JCommander.newBuilder()
		          .addObject(args)
		          .build()
		          .parse(argv);

		VirtualFile.Directory client = IndexVirtualFile.from(ClientMain.class);

		List<VirtualFile.Directory> resourcePacks = new ArrayList<>();

		List<Closeable> closeables = new ArrayList<>();
		List<Throwable> exceptions = new ArrayList<>();
		Throwable firstFailure = null;
		exit:
		try {
			// setup game
			addResourcePack(args.resources, resourcePacks, closeables);
			for(File directory : args.resourcesDirectories) {
				File[] files = requireNonNull(directory.listFiles(), directory + " is not directory!");
				addResourcePack(
					Arrays.asList(files),
					resourcePacks,
					closeables
				);
			}

			resourcePacks.add(client);
			ClientMain.clientResources = OverlayDirectory.overlay("client_resources", resourcePacks);

			// launch game
			if(ClientInit.init(clientResources)) {
				break exit;
			}

			// test code
			World[] worlds = {null};
			ClientWorldServer server = new ClientWorldServer(worlds);
			ClientWorld world = new ClientWorld(server, 0);
			worlds[0] = world;

			ClientChunk value = new ClientChunk(world, 0, 0);
			world.loadedChunkCache.put(0, value);

			ClientChunk test = new ClientChunk(world, 0, -1);
			System.out.println(test);
			world.loadedChunkCache.put(Chunk.combineInts(0, -1), test);
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
			GLFW.glfwSetKeyCallback(ClientInit.glMainWindow, (window, key, scancode, action, mods) -> {
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

			RenderThread.addRenderStage(() -> {
				//Matrix3f mat = ClientInit.cartesianToAWTIndexGrid(8);
				SolidColorShader shader = SolidColorShader.INSTANCE;
				shader.strategy(AutoStrat.LINE_STRIP);
				shader.vert().rgb(0xFFFFFF).vec3f(1, 0, 0);
				shader.vert().rgb(0xFFFFFF).vec3f(1, 1, 0);
				shader.vert().rgb(0xFFFFFF).vec3f(0, 0, 0);
				shader.vert().rgb(0xFFFFFF).vec3f(0, 1, 0);
				//shader.strategy(AutoStrat.sequence(DrawMethod.LINE_LOOP));
				//shader.vert().rgb(0xAAFFFF).vec3f(0, 0, 0);
				//shader.vert().rgb(0xAAFFFF).vec3f(0, -1, 0);
				//shader.vert().rgb(0xAAFFFF).vec3f(-1, 0, 0);
				GL11.glEnable(GL11.GL_CULL_FACE);
				shader.renderAndDelete();
			}, 10);

			/*RenderThread.addRenderStage(() -> {
				Matrix3f mat = new Matrix3f();
				mat.offset(-1, 1);
				mat.scale(2, -2);
				mat.scale(ClientInit.dims[1] / ((float)ClientInit.dims[0]), 1);
				WorldRenderer renderer = new WorldRenderer(world);
				renderer.render(mat, player, scale[0], scale[0]);
			}, 10);*/

			RenderThread.startRender();
			// close game
			ClientChunk.EXECUTOR.shutdown();
		} catch(Throwable e) {
			exceptions.add(e);
		} finally {
			// close game
			for(Closeable closeable : closeables) {
				try {
					closeable.close();
				} catch(IOException e) {
					exceptions.add(e);
				}
			}

			if(!exceptions.isEmpty()) {
				firstFailure = exceptions.get(0);
			}

			for(int i = exceptions.size() - 1; i >= 1; i--) {
				exceptions.get(i).printStackTrace();
			}
		}
		if(firstFailure != null) {
			throw Validate.rethrow(firstFailure);
		}
		GLFW.glfwDestroyWindow(JerrariaClient.MAIN_WINDOW_GL_ID);
	}

	private static void addResourcePack(List<File> args, List<VirtualFile.Directory> resources, List<Closeable> closeables) {
		for(File resource : args) {
			if(!resource.exists()) {
				throw new IllegalArgumentException(resource + " does not exist!");
			}

			Path path = resource.toPath();
			if(Files.isDirectory(path)) {
				resources.add(PathVirtualFile.ofDirectory(path));
			} else {
				try {
					FileSystem system = FileSystems.newFileSystem(path);
					closeables.add(system);
					for(Path directory : system.getRootDirectories()) {
						resources.add(PathVirtualFile.ofDirectory(directory));
					}
				} catch(IOException e) {
					throw new IllegalArgumentException("unable to open archive " + resource);
				}
			}
		}
	}
}
