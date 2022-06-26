package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.math.Mat3f;

public class SolidColorShader extends Shader<Color.ARGB<Vec3.F<End>>> {
	public static final SolidColorShader INSTANCE = create(Id.create("jerraria", "solid_color"), SolidColorShader::new, SolidColorShader::new);

	protected SolidColorShader(VFBuilder<End> builder, Object function) {
		super(builder.add(Vec3.f("pos")).add(Color.argb("color")), function);
	}

	protected SolidColorShader(SolidColorShader shader, SCopy copy) {
		super(shader, copy);
	}

	public void rect(Mat3f mat, float x, float y, float width, float height, int rgb) {
		final float depth = 1f;
		this.strategy(AutoStrat.QUADS);
		this.vert().argb(rgb).vec3f(mat, x, y, depth);
		this.vert().argb(rgb).vec3f(mat, x, y+height, depth);
		this.vert().argb(rgb).vec3f(mat, x+width, y+height, depth);
		this.vert().argb(rgb).vec3f(mat, x+width, y, depth);
	}
}
