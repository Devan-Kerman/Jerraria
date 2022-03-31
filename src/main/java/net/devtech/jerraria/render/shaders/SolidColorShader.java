package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.api.Color;
import net.devtech.jerraria.render.api.End;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.Vec3;

public class SolidColorShader extends Shader<Color.RGB<Vec3.F<End>>> {
	public static final SolidColorShader INSTANCE = createShader(Id.create("jerraria", "solid_color"), SolidColorShader::new, SolidColorShader::new);

	public SolidColorShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Vec3.f("pos")).add(Color.rgb("color")), function);
	}

	public SolidColorShader(SolidColorShader shader, SCopy copy) {
		super(shader, copy);
	}
}
