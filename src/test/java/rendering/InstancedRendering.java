package rendering;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.math.Matrix3f;

public class InstancedRendering {
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			RenderThread.addRenderStage(() -> {
				Matrix3f mat = new Matrix3f();
				InstancedSolidColorShader shader = InstancedSolidColorShader.INSTANCE;
				shader.drawRect(mat, 0, 0, .1f, .1f);
				for(Vec3.F<?> offset : shader.offsets) {
					offset.vec3f((float) Math.random(), (float) Math.random(), 0);
				}

				for(Vec3.F<?> color : shader.colors) {
					color.vec3f(.5f, .5f, .5f);
				}

				shader.drawInstancedKeep(32);
				shader.deleteVertexData();

				InstancedSolidColorShader copy = Shader.copy(shader, SCopy.PRESERVE_BOTH);
				copy.drawInstancedKeep(32);

				for(Vec3.F<?> offset : shader.offsets) {
					offset.vec3f((float) Math.random(), (float) Math.random(), 0);
				}

				for(Vec3.F<?> color : shader.colors) {
					color.vec3f(.5f, 1, 1);
				}

				shader.drawRect(mat, 0, 0, .1f, .1f);
				shader.drawInstancedKeep(32);
				shader.deleteVertexData();
			}, 10);
			return null;
		});
	}
}
