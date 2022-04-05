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
import net.devtech.jerraria.client.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.client.render.api.Primitive;
import net.devtech.jerraria.client.render.textures.Texture;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.PathVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;

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
			ColoredTextureShader text = ColoredTextureShader.INSTANCE;
			text.flush();
			text.texture.atlas(ClientInit.mainAtlas);
			Texture texture = ClientInit.mainAtlas.asTexture();
			text.vert().vec3f(-1, -1, 1).uv(texture,0, 1).rgb(0xFFFFFF);
			text.vert().vec3f(1, -1, 1).uv(texture,1, 1).rgb(0xFFFFFF);
			text.vert().vec3f(-1, 1, 1).uv(texture,0, 0).rgb(0xFFFFFF);

			text.vert().vec3f(1, 1, 1).uv(texture,1, 0).rgb(0xFFFFFF);
			text.vert().vec3f(1, -1, 1).uv(texture,1, 1).rgb(0xFFFFFF);
			text.vert().vec3f(-1, 1, 1).uv(texture,0, 0).rgb(0xFFFFFF);
			RenderThread.addRenderStage(() -> text.render(Primitive.TRIANGLE), 10);
			// test code

			RenderThread.startRender();

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
