package net.devtech.jerraria.client.main;

import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.devtech.jerraria.client.render.VAO;
import net.devtech.jerraria.client.render.ShaderParser;
import net.devtech.jerraria.client.render.glsl.ast.declaration.ExternalFieldDeclarationAstNode;
import net.devtech.jerraria.client.render.types.DataType;
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
			in vec3 aPos;
			uniform Light {
				vec4 color;
				vec4 test;
			};
			out vec4 vertexColor;

			void main() {
			    gl_Position = vec4(aPos, 1.0);
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

		ShaderParser parser = new ShaderParser(vertexSrc);

		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexSrc);
		glCompileShader(vertexShader);
		int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragShader, fragmentSrc);
		glCompileShader(fragShader);
		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragShader);
		glLinkProgram(program);

		int[] input = {0};
		glGetProgramiv(program, GL_LINK_STATUS, input);
		if(input[0] == 0) {
			System.err.println(glGetProgramInfoLog(program));
		}
		glDeleteShader(vertexShader);
		glDeleteShader(fragShader);

		ByteBuffer liberal = ByteBuffer.allocateDirect(4);
		liberal.putInt(0xFFFFFFFF);
		int buffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, buffer);
		glBufferData(GL_UNIFORM_BUFFER, liberal, GL_STATIC_DRAW);
		int lightsIndex = glGetUniformBlockIndex(program, "Light");
		glUniformBlockBinding(program, lightsIndex, 0);
		System.out.println(glGetUniformIndices(program, "color"));
		System.out.println(glGetUniformIndices(program, "test"));

		System.out.println(parser.getFields(ExternalFieldDeclarationAstNode.ExternalFieldType.UNIFORM));

		VAO vao = VAO.getOrCreateDefaultGroup(Map.of("aPos", DataType.NORMALIZED_F8_VEC3), parser, program);
		VAO.Element pos = vao.elements().get("aPos"); //, color = vao.quick().color();
		byte zero = 0, max = -1;
		vao.start();
		vao.element(pos).put(zero).put(zero).put(zero);
		//vao.element(color).putInt(0xFFFFFF00);
		vao.next();
		vao.element(pos).put(max).put(zero).put(zero);
		//vao.element(color).putInt(0xFFFFFF00);
		vao.next();
		vao.element(pos).put(max).put(max).put(zero);
		//vao.element(color).putInt(0xFFFFFF00);
		vao.next();

		while(!GLFW.glfwWindowShouldClose(window)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			glUseProgram(program);
			vao.bindAndDraw(GL_TRIANGLES);

			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		GLFW.glfwTerminate();
	}

	private static void render() {
		// todo: render
	}
}
