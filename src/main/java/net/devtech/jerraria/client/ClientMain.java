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
import net.devtech.jerraria.loading.LoadingStage;
import net.devtech.jerraria.render.ClientRenderContext;
import net.devtech.jerraria.render.math.Matrix3f;
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.api.Primitive;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.PathVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ClientMain {
	public static void main(String[] argv) {
		ClientArgs args = new ClientArgs();
		JCommander.newBuilder()
		          .addObject(args)
		          .build()
		          .parse(argv);

		VirtualFile.Directory client = IndexVirtualFile.from(ClientMain.class);

		List<VirtualFile.Directory> resourcePacks = new ArrayList<>();

		List<Closeable> closeables = new ArrayList<>();
		List<IOException> exceptions = new ArrayList<>();
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

			OverlayDirectory clientResources = OverlayDirectory.overlay("client", resourcePacks);
			// launch game
			ClientRenderContext.initializeRendering(clientResources);

			SolidColorShader box = SolidColorShader.INSTANCE;
			ColoredTextureShader text = ColoredTextureShader.INSTANCE;
			text.texture.tex(ClientRenderContext.asciiAtlasId);

			LoadingStage stage = LoadingStage.create("loading", 10);
			stage.complete(5);

			stage.allocateSubstage("atlas", 4).complete(2);
			stage.allocateSubstage("sound", 9).complete(2);

			while(!GLFW.glfwWindowShouldClose(ClientRenderContext.glMainWindow)) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				int[] dims = ClientRenderContext.dims;
				Matrix3f mat = new Matrix3f();
				mat.scale(dims[1] / (dims[0] * 8F), 1/8F);
				mat.scale(2, -2);
				mat.translate(-1, 1);

				stage.render(mat, box, text, 10, 0, 0);

				box.renderAndFlush(Primitive.TRIANGLE);
				text.renderAndFlush(Primitive.TRIANGLE);
				GLFW.glfwSwapBuffers(ClientRenderContext.glMainWindow);
				GLFW.glfwPollEvents();
			}

			// close game

		} catch(IOException e) {
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

			for(IOException exception : exceptions) {
				exception.printStackTrace();
			}
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
