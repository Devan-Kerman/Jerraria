package rendering;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.KeyCopying;
import net.devtech.jerraria.render.api.instanced.Instancer;
import net.devtech.jerraria.util.func.TRunnable;

public class SSBORendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			SSBOShader model = SSBOShader.INSTANCE;
			// create the instance model, in this case it's just a square
			model.strategy(AutoStrat.QUADS);
			model.vert().vec3f(0, 0, 0);
			model.vert().vec3f(0, 1, 0);
			model.vert().vec3f(1, 1, 0);
			model.vert().vec3f(1, 0, 0);

			Instancer<SSBOShader> instancer = Instancer.simple((from, to) -> {
				// this lambda describes how instances are copied from one part to another
				// I will add an api to allow copying entire structs at once for performance soon ish
				// this is needed for array compacting basically

				// I elected for explicit copying to make the API more generic
					// I didn't want to restrict the API to 1 struct per instance
				KeyCopying.ssbo(from, to, s -> s.instances);
			}, model, 10);

			List<InstanceKey<?>> keys = new ArrayList<>();
			for(int i = 0; i < 24; i++) {
				keys.add(instancer.getOrAllocateId());
			}

			InstanceKey<SSBOShader> id = instancer.getOrAllocateId(); // create instance
			id.ssbo(s -> s.instances).color.vec4f(.5f, .0f, .0f, .5f); // set instance field
			//id.ssbo(s -> s.instances).scale.f(1);

			keys.forEach(InstanceKey::invalidate);
			// id.invalidate(); invalidate the id, this deallocates the instance and stops it from being rendered
			// id.addHeartbeat(i -> myThing.isValid()); adds a condition for this instance, this is called every frame and is used to check if the instance is still valid

			RenderThread.addRenderStage(TRunnable.of(() -> {
				id.ssbo(s -> s.instances).scale.f((float) Math.sin(System.currentTimeMillis() / 100d));

				// render the instances
				for(Instancer.Block<SSBOShader> block : instancer.compactAndGetBlocks()) {
					block.block().fade.vec4f(.5f, .5f, .5f, .5f);
					block.block().drawInstancedKeep(block.instances());
				}
			}), 10);

			return null;
		});
	}
}
