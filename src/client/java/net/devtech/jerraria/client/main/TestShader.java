package net.devtech.jerraria.client.main;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.client.render.api.types.End;
import net.devtech.jerraria.client.render.api.SCopy;
import net.devtech.jerraria.client.render.api.Shader;
import net.devtech.jerraria.client.render.api.types.V;
import net.devtech.jerraria.client.render.api.VFBuilder;
import net.devtech.jerraria.client.render.api.types.Vec3;

public class TestShader extends Shader<Vec3.F<End>> {
	public static final TestShader INSTANCE = createShader(Id.create("bruh", "test"), TestShader::new, TestShader::new);

	public final V.F<?> w = this.uni(V.f("w"));
	public final Vec3.F<?> color = this.uni(Vec3.f("color"));

	public TestShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Vec3.f("aPos")), function);
	}


	public TestShader(TestShader shader, SCopy copy) {
		super(shader, copy);
	}
}
