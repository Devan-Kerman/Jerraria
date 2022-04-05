package net.devtech.jerraria.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.client.render.api.Primitive;
import net.devtech.jerraria.client.render.internal.ShaderManager;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.client.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.client.render.shaders.SolidColorShader;
import net.devtech.jerraria.client.render.textures.Atlas;
import net.devtech.jerraria.client.render.textures.Texture;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.collect.RandomCollection;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

class ClientInit {
	static int maxTextureSize;
	static long glMainWindow;
	static RandomCollection<String> titleTextCollection;
	static String title;
	static int asciiAtlasId;
	static Atlas mainAtlas;
	static int[] dims = {800, 600};

	static boolean init(VirtualFile.Directory directory) {
		// handled by static block
		titleTextCollection = readSplashText(directory, "boot/title.txt");
		GLFW.glfwInit();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

		String title = titleTextCollection.next();
		ClientInit.title = title;

		long window = GLFW.glfwCreateWindow(800, 600, title, MemoryUtil.NULL, MemoryUtil.NULL);
		glMainWindow = window;
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities();

		maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);

		// todo save viewport size
		GL11.glViewport(0, 0, 800, 600);
		GLFW.glfwSetFramebufferSizeCallback(window, ($, width, height) -> {
			GL11.glViewport(0, 0, width, height);
			ClientInit.dims = new int[]{width, height};
		});

		ShaderManager.FRAG_SOURCES.add(shaderId -> findShaderSource(directory, shaderId, ".frag"));
		ShaderManager.VERT_SOURCES.add(shaderId -> findShaderSource(directory, shaderId, ".vert"));

		ShaderManager.SHADER_PROVIDERS.add(id -> { // shaderid.properties allows you to reuse frag/vertex shader files.
			VirtualFile shaders = directory
				.resolveDirectory(id.unpackNamespace())
				.resolveDirectory("shaders")
				.resolve(id.getUnpackedPath() + "." + Validate.PROPERTIES_FILE_EXTENSION);
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
		try {
			asciiAtlasId = Texture.loadTexture(directory, "boot/ascii_atlas.png").getGlId();
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}

		SolidColorShader box = SolidColorShader.INSTANCE;
		ColoredTextureShader text = ColoredTextureShader.INSTANCE;
		text.texture.tex(ClientInit.asciiAtlasId);

		List<Runnable> renderThreadTasks = new Vector<>();
		Executor renderThreadExecutor = renderThreadTasks::add;
		LoadRender initializationProgress = new LoadRender(null, "Game Initialization [%d/%d]", 1);
		LoadRender atlasProgress = initializationProgress.substage("Stitching main atlas", 1);

		CompletableFuture<Atlas> mainAtlas = CompletableFuture.supplyAsync(() -> Atlas.createAtlas(atlasProgress, renderThreadExecutor, Id.parse("jerraria:main")));
		CompletableFuture<?> gameInitialization = CompletableFuture.allOf(mainAtlas);

		// loading screen
		boolean exit;
		while(!((exit = GLFW.glfwWindowShouldClose(ClientInit.glMainWindow)) || gameInitialization.isDone())) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			int[] dims = ClientInit.dims;

			Matrix3f cartToIndexMat = new Matrix3f();
			cartToIndexMat.offset(-1, 1);
			cartToIndexMat.scale(2, -2);
			cartToIndexMat.scale(dims[1] / (dims[0] * 8F), 1 / 8F);

			initializationProgress.render(cartToIndexMat, box, text, 10, 0, 0);
			box.renderAndFlush(Primitive.TRIANGLE);
			text.renderAndFlush(Primitive.TRIANGLE);
			GLFW.glfwSwapBuffers(ClientInit.glMainWindow);
			GLFW.glfwPollEvents();

			for(int i = renderThreadTasks.size() - 1; i >= 0; i--) {
				renderThreadTasks.remove(i).run();
			}
		}

		if(exit) {
			// maybe window should close and start up again later
			mainAtlas.thenAccept(a -> ClientInit.mainAtlas = a);
		} else {
			ClientInit.mainAtlas = mainAtlas.join();
		}
		return exit;
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
