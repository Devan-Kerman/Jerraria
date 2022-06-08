package net.devtech.jerraria.client;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL11C.GL_LESS;

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
import java.util.concurrent.Callable;

import com.beust.jcommander.JCommander;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.api.GlStateStack;
import net.devtech.jerraria.render.api.impl.RenderingEnvironmentInternal;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.PathVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.world.internal.client.ClientChunk;
import org.lwjgl.glfw.GLFW;

public class Bootstrap {
	public static OverlayDirectory clientResources;
	public static Thread renderThread;

	public static void startClient(String[] argv, Callable<?> run) {
		ClientArgs args = new ClientArgs();
		JCommander.newBuilder().addObject(args).build().parse(argv);
		RenderingEnvironmentInternal.renderThread_ = Thread.currentThread();
		startClient(args, run);
	}

	public static void startClient(ClientArgs argv, Callable<?> run) {
		VirtualFile.Directory client = IndexVirtualFile.from(ClientMain.class);
		List<VirtualFile.Directory> resourcePacks = new ArrayList<>();
		List<Closeable> closeables = new ArrayList<>();
		List<Throwable> exceptions = new ArrayList<>();
		Throwable firstFailure = null;
		exit:
		try {
			// setup game
			addResourcePack(argv.resources, resourcePacks, closeables);
			for(File directory : argv.resourcesDirectories) {
				File[] files = requireNonNull(directory.listFiles(), directory + " is not directory!");
				addResourcePack(Arrays.asList(files), resourcePacks, closeables);
			}

			resourcePacks.add(client);
			clientResources = OverlayDirectory.overlay("client_resources", resourcePacks);

			// launch game
			if(ClientInit.init(clientResources)) {
				break exit;
			}

			// test code
			run.call();

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

	static void addResourcePack(List<File> args, List<VirtualFile.Directory> resources, List<Closeable> closeables) {
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
