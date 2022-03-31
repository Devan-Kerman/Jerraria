package net.devtech.jerraria.render;

import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.stream.Collectors;

import de.matthiasmann.twl.utils.PNGDecoder;
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

public class ClientRenderContext {
	public static long glMainWindow;
	public static RandomCollection<String> titleTextCollection;
	public static String title;
	public static int asciiAtlasId;
	public static int[] dims = {800, 600};

	public static void initializeRendering(VirtualFile.Directory directory) throws IOException {
		// handled by static block
		titleTextCollection = readSplashText(directory, "title.txt");
		GLFW.glfwInit();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

		String title = titleTextCollection.next();
		long window = GLFW.glfwCreateWindow(800, 600, title, MemoryUtil.NULL, MemoryUtil.NULL);
		ClientRenderContext.title = title;
		glMainWindow = window;
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities();

		// todo save viewport size
		GL11.glViewport(0, 0, 800, 600);
		GLFW.glfwSetFramebufferSizeCallback(window, ($, width, height) -> {
			GL11.glViewport(0, 0, width, height);
			ClientRenderContext.dims = new int[]{width, height};
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
		asciiAtlasId = loadBootTexture(directory, "boot/ascii_atlas.png");
	}

	public static int loadBootTexture(VirtualFile.Directory directory, String texture) throws IOException {
		// load png file
		VirtualFile.Regular regular = directory
			.resolveFile(texture);

		PNGDecoder decoder = new PNGDecoder(regular.read());

		//create a byte buffer big enough to store RGBA values
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());

		//decode
		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);

		//flip the buffer so its ready to read
		buffer.flip();

		//create a texture
		int id = glGenTextures();

		//bind the texture
		glBindTexture(GL_TEXTURE_2D, id);

		//tell opengl how to unpack bytes
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		//set the texture parameters, can be GL_LINEAR or GL_NEAREST
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		//upload texture
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return id;
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
