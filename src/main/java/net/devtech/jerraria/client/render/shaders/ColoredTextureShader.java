package net.devtech.jerraria.client.render.shaders;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.client.render.api.Color;
import net.devtech.jerraria.client.render.api.End;
import net.devtech.jerraria.client.render.api.SCopy;
import net.devtech.jerraria.client.render.api.Shader;
import net.devtech.jerraria.client.render.api.Tex;
import net.devtech.jerraria.client.render.api.VFBuilder;
import net.devtech.jerraria.client.render.api.Vec2;
import net.devtech.jerraria.client.render.api.Vec3;

public class ColoredTextureShader extends Shader<Vec3.F<Vec2.F<Color.RGB<End>>>> {
	public static final ColoredTextureShader INSTANCE = createShader(Id.create("jerraria", "colored_texture"), ColoredTextureShader::new, ColoredTextureShader::new);
	public final Tex<?> texture = this.uni(Tex.tex2d("texture_"));

	public ColoredTextureShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Color.rgb("color")).add(Vec2.f("uv")).add(Vec3.f("pos")), function);
	}

	public ColoredTextureShader(ColoredTextureShader shader, SCopy copy) {
		super(shader, copy);
	}
}

