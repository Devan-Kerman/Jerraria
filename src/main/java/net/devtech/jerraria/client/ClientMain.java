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
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.api.Primitive;
import net.devtech.jerraria.render.textures.Texture;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.PathVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ClientMain {
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
			ClientMain.clientResources = OverlayDirectory.overlay("client", resourcePacks);

			// launch game

			ClientRenderContext.init();

			ColoredTextureShader text = ColoredTextureShader.INSTANCE;
			text.flush();
			text.texture.atlas(ClientRenderContext.mainAtlas);
			Texture texture = ClientRenderContext.mainAtlas.asTexture();
			text.vert().vec3f(-1, -1, 1).uv(texture,0, 1).rgb(0xFFFFFF);
			text.vert().vec3f(1, -1, 1).uv(texture,1, 1).rgb(0xFFFFFF);
			text.vert().vec3f(-1, 1, 1).uv(texture,0, 0).rgb(0xFFFFFF);

			text.vert().vec3f(1, 1, 1).uv(texture,1, 0).rgb(0xFFFFFF);
			text.vert().vec3f(1, -1, 1).uv(texture,1, 1).rgb(0xFFFFFF);
			text.vert().vec3f(-1, 1, 1).uv(texture,0, 0).rgb(0xFFFFFF);

			while(!GLFW.glfwWindowShouldClose(ClientRenderContext.glMainWindow)) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				text.render(Primitive.TRIANGLE);
				GLFW.glfwSwapBuffers(ClientRenderContext.glMainWindow);
				GLFW.glfwPollEvents();
				ClientRenderContext.mainAtlas.updateAnimation(System.currentTimeMillis());
			}

			// close game

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
