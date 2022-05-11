package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.math.Matrix3f;

public class InstancedSolidColorShader extends Shader<Color.RGB<Vec3.F<End>>> {
	public static final InstancedSolidColorShader INSTANCE = createShader(Id.create("jerraria", "test_instanced"), InstancedSolidColorShader::new, InstancedSolidColorShader::new);
	public final Vec3.F<?>[] offsets = new Vec3.F[32];

	public InstancedSolidColorShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Vec3.f("pos")).add(Color.rgb("color")), function);
		this.init();
	}

	public InstancedSolidColorShader(InstancedSolidColorShader shader, SCopy copy) {
		super(shader, copy);
		this.init();
	}

	protected void init() {
		for(int i = 0; i < this.offsets.length; i++) {
			this.offsets[i] = this.uni(Vec3.f(String.format("offsets[%d]", i), "data"));
		}
	}

	/**
	 * draws a rectangle using triangles
	 */
	public void drawRect(Matrix3f mat, float x, float y, float width, float height, int rgb) {
		this.vert().rgb(rgb).vec3f(mat, x, y, 1);
		this.vert().rgb(rgb).vec3f(mat, x+width, y, 1);
		this.vert().rgb(rgb).vec3f(mat, x, y+height, 1);

		this.vert().rgb(rgb).vec3f(mat, x+width, y+height, 1);
		this.vert().rgb(rgb).vec3f(mat, x+width, y, 1);
		this.vert().rgb(rgb).vec3f(mat, x, y+height, 1);
	}
}
