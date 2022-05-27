package rendering;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;
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
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.shaders.LLTransRecordShader;
import net.devtech.jerraria.render.shaders.LLTransResolveShader;
import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.util.math.Matrix3f;

public class LinkedListTranslucencyRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			int translucencyBuffer = allocateTranslucencyBuffer(800, 600, 16); // doesn't need to get cleared
			RenderThread.addRenderStage(() -> {
				int imageListHead = allocateImageListHead(802, 602);
				LLTransRecordShader rendering = LLTransRecordShader.INSTANCE;
				rendering.counter.ui(0L);
				rendering.translucencyBuffer.tex(translucencyBuffer);
				rendering.imgListHead.tex(imageListHead);

				Matrix3f identity = new Matrix3f();

				int[] colors = {0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
				                0x0000FF, 0xFF00FF, 0xFFFFFF, 0xAAAAAA};
				for(float i = 0; i < 1; i+=.125) {
					rendering.square(identity, i/2f, i/2f, .5f, .5f, -i, 0x80000000 | colors[(int) (i * 8)]);
				}

				// render to translucency buffer
				rendering.drawKeep();
				rendering.deleteVertexData();
				glMemoryBarrier(-1);
				System.out.println(rendering.counter.getValue());

				LLTransResolveShader shader = LLTransResolveShader.INSTANCE;
				shader.translucencyBuffer.tex(translucencyBuffer);
				shader.imgListHead.tex(imageListHead);
				shader.strategy(AutoStrat.QUADS);
				shader.vert().vec3f(0, 0, 0);
				shader.vert().vec3f(0, 1, 0);
				shader.vert().vec3f(1, 1, 0);
				shader.vert().vec3f(1, 0, 0);
				shader.drawKeep();
				shader.deleteVertexData();

				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
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
		glBufferData(type, size, GL_STATIC_DRAW);
		glBindTexture(type, tex);
		glTexBuffer(type, GL_RGBA32UI, buf);
		return tex;
	}

	public static int allocateImageListHead(int width, int height) {
		int tex = glGenTextures();
		int type = DataType.UINT_IMAGE_2D.elementType;
		glBindTexture(type, tex);
		glTexStorage2D(type, 1, GL_R32UI, width, height);
		return tex;
	}
}
