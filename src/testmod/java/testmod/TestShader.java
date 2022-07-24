package testmod;

import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.ShaderBuffer;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.InstanceManager;
import net.devtech.jerraria.render.api.instanced.KeyCopying;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Mat4;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;

public class TestShader extends MinecraftShader<Vec3.F<End>> {
	public static final TestShader INSTANCE = MinecraftShader.createMinecraft(Id.create("jerraria", "test_shader"),
		TestShader::new,
		TestShader::new
	);
	static {
		INSTANCE.strategy(AutoStrat.QUADS);
	}

	public final ShaderBuffer<Mat4.x4<End>> blockEntityMats = this.buffer("Mats.blockEntityMat[%d]", Mat4::mat4);

	protected TestShader(Builder<End> builder, Object context) {
		super(builder.add(pos("Position")), context);
	}

	public TestShader(TestShader shader, SCopy method) {
		super(shader, method);
	}
}
