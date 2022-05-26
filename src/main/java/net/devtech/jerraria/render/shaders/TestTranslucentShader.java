package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.translucency.TranslucencyStrategy;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;

public class TestTranslucentShader extends TranslucentShader<Vec3.F<Color.ARGB<End>>> {
	public static final TestTranslucentShader INSTANCE = createShader(
		Id.create("jerraria", "translucency_test"),
		TestTranslucentShader::new,
		TestTranslucentShader::new
	);

	protected TestTranslucentShader(Id id, VFBuilder<End> builder, Object context) {
		super(id, builder.add(Color.argb("color")).add(Vec3.f("pos")), context, TranslucentShaderType.SINGLE_PASS);
	}

	protected TestTranslucentShader(Shader<Vec3.F<Color.ARGB<End>>> shader, SCopy method) {
		super(shader, method, TranslucentShaderType.SINGLE_PASS);
	}

	public void square(Matrix3f mat, float offX, float offY, float width, float height, float z, int color) {
		this.vert().vec3f(mat, offX, offY, z).argb(color);
		this.vert().vec3f(mat, offX, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY, z).argb(color);
	}
}
