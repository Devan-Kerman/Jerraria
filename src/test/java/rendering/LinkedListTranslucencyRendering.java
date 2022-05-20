package rendering;

import static org.lwjgl.opengl.GL42.GL_R32UI;
import static org.lwjgl.opengl.GL42.GL_RGBA32UI;
import static org.lwjgl.opengl.GL42.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL42.glBindBuffer;
import static org.lwjgl.opengl.GL42.glBindTexture;
import static org.lwjgl.opengl.GL42.glBufferData;
import static org.lwjgl.opengl.GL42.glGenBuffers;
import static org.lwjgl.opengl.GL42.glGenTextures;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL42.glTexBuffer;
import static org.lwjgl.opengl.GL42.glTexStorage2D;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.shaders.LLTransRecordShader;
import net.devtech.jerraria.render.shaders.LLTransResolveShader;
import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.util.math.Matrix3f;

public class LinkedListTranslucencyRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	// todo unhardcode types in Uniform
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			int translucencyBuffer = allocateTranslucencyBuffer(800, 600, 32);
			RenderThread.addRenderStage(() -> {
				// only this needs to be cleared
				int imageListHead = allocateImageListHead(802, 602);
				LLTransRecordShader rendering = LLTransRecordShader.INSTANCE;
				rendering.counter.ui(0L);
				rendering.translucencyBuffer.tex(translucencyBuffer);
				rendering.imgListHead.tex(imageListHead);
				//GL46.glClearTexImage(imageListHead, 0, GL_RED, GL_UNSIGNED_INT, (ByteBuffer) null);

				Matrix3f identity = new Matrix3f();

				rendering.square(identity, 0, 0, 1, 1, 1, 0x800000FF);
				rendering.square(identity, 0, 0, .5f, .5f, -1, 0x80FF0000);

				// render to translucency buffer
				rendering.render();
				rendering.deleteVertexData();
				glMemoryBarrier(-1);

				LLTransResolveShader shader = LLTransResolveShader.INSTANCE;
				shader.translucencyBuffer.tex(translucencyBuffer);
				shader.imgListHead.tex(imageListHead);
				shader.vert().vec3f(0, 0, 0);
				shader.vert().vec3f(0, 1, 0);
				shader.vert().vec3f(1, 0, 0);
				shader.vert().vec3f(1, 0, 0);
				shader.vert().vec3f(0, 1, 0);
				shader.vert().vec3f(1, 1, 0);
				shader.render();
				shader.deleteVertexData();
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				//glDeleteTextures(imageListHead);
			}, 10);

			return null;
		});
	}

	public static int allocateTranslucencyBuffer(int width, int height, int K) {
		int tex = glGenTextures();
		int buf = glGenBuffers();
		int type = DataType.UINT_IMAGE_BUFFER.elementType;
		glBindBuffer(type, buf);
		int size = JMath.nearestPowerOf2(width * height * K * 16);
		System.out.println(size);
		glBufferData(type, size, GL_STATIC_DRAW);
		glBindTexture(type, tex);
		glTexBuffer(type, GL_RGBA32UI, buf);
		return tex;
	}

	public static int allocateImageListHead(int width, int height) {
		int id = glGenTextures();
		int type = DataType.UINT_IMAGE_2D.elementType;
		glBindTexture(type, id);
		glTexStorage2D(type, 1, GL_R32UI, width, height);
		return id;
	}
}
