package net.devtech.jerraria.client.main;

import static org.lwjgl.opengl.GL20.glUniform1iv;
import static org.lwjgl.opengl.GL31.GL_TRIANGLES;
import static org.lwjgl.opengl.GL31.glBufferData;
import static org.lwjgl.opengl.GL31.glGenBuffers;
import static org.lwjgl.opengl.GL31.glGetActiveUniformBlockName;
import static org.lwjgl.opengl.GL31.glGetActiveUniformName;
import static org.lwjgl.opengl.GL31.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL31.glGetProgramiv;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glGetUniformIndices;
import static org.lwjgl.opengl.GL31.glShaderSource;

import java.util.List;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.UniformData;
import net.devtech.jerraria.render.internal.VAO;
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
		String vertexSrc = """
			#version 330 core

			uniform Uniform { vec3 color; };
			uniform float w;

			in vec3 aPos;
			out vec3 vertexColor;

			void main() {
			    gl_Position = vec4(aPos, w);
			    vertexColor = color;
			}
			""".stripLeading();

		@Language("GLSL")
		String fragmentSrc = """
			#version 330 core
			out vec4 FragColor;
			in vec4 vertexColor;

			void main() {
			    FragColor = vertexColor;
			}
			""".stripLeading();

		Id id = Id.create("bruh", "test");
		BareShader shader = BareShader.compileShaders(i -> fragmentSrc, i -> vertexSrc, List.of(
			new BareShader.Uncompiled(
				id,
				id,
				id)
				.vert(DataType.F32_VEC3, "aPos")
				.uniform(DataType.F32_VEC3, "color", "Uniform")
				.uniform(DataType.F32, "w")
		)).get(id);

		VAO vao = shader.vao;
		vao.start();
		var pos = vao.getElement("aPos");
		vao.element(pos).f(0).f(0).f(0);
		vao.next();
		vao.element(pos).f(1).f(0).f(0);
		vao.next();
		vao.element(pos).f(0).f(1).f(0);
		vao.next();

		UniformData uniforms = shader.uniforms;
		uniforms.start();
		uniforms.element("color").f(1.0f).f(0.5f).f(1.0f);
		uniforms.element("w").f(1.0f);

		BareShader copy = new BareShader(shader);
		VAO vao2 = copy.vao;
		vao2.start();
		var pos2 = vao2.getElement("aPos");
		vao2.element(pos2).f(0).f(0).f(0);
		vao2.next();
		vao2.element(pos2).f(1).f(0).f(0);
		vao2.next();
		vao2.element(pos2).f(0).f(1).f(0);
		vao2.next();

		UniformData uniforms2 = copy.uniforms;
		uniforms2.start();
		uniforms2.element("color").f(1.0f).f(0.5f).f(1.0f);
		uniforms2.element("w").f(1.0f);

		while(!GLFW.glfwWindowShouldClose(window)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			//shader.draw(GL_TRIANGLES);
			copy.draw(GL_TRIANGLES);
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		GLFW.glfwTerminate();
	}

	private static void render() {
		// todo: render
	}
}
