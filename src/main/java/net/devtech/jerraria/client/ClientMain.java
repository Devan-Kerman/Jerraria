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

			/*SolidColorShader shader = SolidColorShader.INSTANCE;
			shader.vert().rgb(255, 255, 255).vec3f(0, 0, 0);
			shader.vert().rgb(255, 255,255).vec3f(1, 0, 0);
			shader.vert().rgb(255, 255, 255).vec3f(0, 1, 0);*/

			ColoredTextureShader shader = ColoredTextureShader.INSTANCE;
			shader.texture.tex(ClientRenderContext.asciiAtlasId);


			while(!GLFW.glfwWindowShouldClose(ClientRenderContext.glMainWindow)) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				int[] dims = ClientRenderContext.dims;
				Matrix3f mat = new Matrix3f();
				mat.scale(2, 2);
				mat.translate(-1, -1);
				mat.scale(dims[1] / (dims[0] * 8F), 1/8F);
				// todo make matrix
				LoadingStage.renderText(mat, shader, "ur kinda cringe bro");
				shader.renderAndFlush(Primitive.TRIANGLE);
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
