package net.devtech.jerraria.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.RandomCollection;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class ClientRendering {
	public static long glMainWindow;
	public static RandomCollection<String> titleTextCollection;
	public static String title;

	public static void initializeRendering(VirtualFile.Directory directory) {
		// handled by static block
		titleTextCollection = readSplashText(directory, "title.txt");
		GLFW.glfwInit();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

		String title = titleTextCollection.next();
		long window = GLFW.glfwCreateWindow(800, 600, title, MemoryUtil.NULL, MemoryUtil.NULL);
		ClientRendering.title = title;
		glMainWindow = window;
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities();

		// todo save viewport size
		GL11.glViewport(0, 0, 800, 600);
		GLFW.glfwSetFramebufferSizeCallback(window, ($, width, height) -> {
			GL11.glViewport(0, 0, width, height);
		});

		ShaderManager.FRAG_SOURCES.add(shaderId -> findShaderSource(directory, shaderId, ".frag"));
		ShaderManager.VERT_SOURCES.add(shaderId -> findShaderSource(directory, shaderId, ".vert"));

		ShaderManager.SHADER_PROVIDERS.add(id -> { // shaderid.properties allows you to reuse frag/vertex shader files.
			VirtualFile shaders = directory
				.resolveDirectory(id.unpackNamespace())
				.resolveDirectory("shaders")
				.resolve(id.getUnpackedPath() + ".properties");
			if(shaders != null) {
				try(var input = shaders.asRegular().read()) {
					Properties properties = new Properties();
					properties.load(input);
					String frag = properties.getProperty("frag");
					String vert = properties.getProperty("vert");
					return new ShaderManager.ShaderPair(Id.parse(frag), Id.parse(vert));
				} catch(IOException e) {
					throw new IllegalArgumentException("Unable to parse " + shaders.name(), e);
				}
			}
			return null;
		});
		ShaderManager.SHADER_PROVIDERS.add(id -> new ShaderManager.ShaderPair(id, id));
	}

	@NotNull
	private static String findShaderSource(VirtualFile.Directory directory, Id shaderId, String extension) {
		VirtualFile.Regular shader = directory
			.resolveDirectory(shaderId.unpackNamespace())
			.resolveDirectory("shaders")
			.resolveFile(shaderId.getUnpackedPath() + extension);
		String source;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(shader.read()))) {
			source = reader.lines().collect(Collectors.joining("\n"));
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
		return source;
	}

	public static RandomCollection<String> readSplashText(VirtualFile.Directory directory, String fileName) {
		RandomCollection<String> collection = new RandomCollection<>();
		for(VirtualFile file : directory.resolveAll(fileName)) {
			VirtualFile.Regular regular = file.asRegular();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(regular.read()))) {
				reader.lines().forEach(s -> {
					String[] split = s.split("\\#", 2);
					double weight = Double.parseDouble(split[0]);
					collection.add(weight, split[1]);
				});
			} catch(IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return collection;
	}
}
