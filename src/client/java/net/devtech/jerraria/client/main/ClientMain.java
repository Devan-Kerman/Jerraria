package net.devtech.jerraria.client.main;

import net.devtech.jerraria.client.render.api.Primitive;
import net.devtech.jerraria.client.render.api.SCopy;
import net.devtech.jerraria.client.render.api.Shader;
import net.devtech.jerraria.client.render.internal.ShaderManager;
import org.intellij.lang.annotations.Language;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class ClientMain {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		GLFW.glfwInit();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

		long window = GLFW.glfwCreateWindow(800, 600, "Jerraria: A new crab", MemoryUtil.NULL, MemoryUtil.NULL);
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(window);

		GL.createCapabilities();
		GL11.glViewport(0, 0, 800, 600);

		GLFW.glfwSetFramebufferSizeCallback(window, ($, width, height) -> {
			GL11.glViewport(0, 0, width, height);
		});

		@Language("GLSL")
		String fragmentSrc = """
			#version 330 core
			out vec4 FragColor;
			in vec4 vertexColor;

			void main() {
			    FragColor = vertexColor;
			}
			""".stripLeading();

		@Language("GLSL")
		String vertexSrc = """
			#version 430 core

			in vec3 aPos;

			uniform vec3 color;
			uniform float w;

			out vec3 vertexColor;
			void main() {
			    gl_Position = vec4(aPos, w);
			    vertexColor = vec3(color.rg, w * color.b);
			}
			""".stripLeading();

		ShaderManager.FRAG_SOURCES.add($ -> fragmentSrc);
		ShaderManager.VERT_SOURCES.add($ -> vertexSrc);
		ShaderManager.SHADER_PROVIDERS.add($ -> new ShaderManager.ShaderPair($, $));

		TestShader shader = TestShader.INSTANCE;
		shader.color.vec3f(1.0f, 0.5f, .5f);
		shader.w.f(1.0f);

		shader.vert().vec3f(0, 0, 0);
		shader.vert().vec3f(1, 0, 0);
		shader.vert().vec3f(0, 1, 0);

		TestShader copy = Shader.copy(shader, SCopy.PRESERVE_VERTEX_DATA);
		copy.color.vec3f(1.0f, 0.5f, .5f);
		copy.w.f(1f);

		//shader.vert().vec3f(0, 0, 0);
		//shader.vert().vec3f(-1, 0, 0);
		//shader.vert().vec3f(0, -1, 0);

		while(!GLFW.glfwWindowShouldClose(window)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			copy.render(Primitive.TRIANGLE);
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		GLFW.glfwTerminate();
	}
}
