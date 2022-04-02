package net.devtech.jerraria.client.render.shaders;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.client.render.textures.Texture;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.client.render.api.types.Color;
import net.devtech.jerraria.client.render.api.types.End;
import net.devtech.jerraria.client.render.api.SCopy;
import net.devtech.jerraria.client.render.api.Shader;
import net.devtech.jerraria.client.render.api.types.Tex;
import net.devtech.jerraria.client.render.api.VFBuilder;
import net.devtech.jerraria.client.render.api.types.Vec2;
import net.devtech.jerraria.client.render.api.types.Vec3;

public class ColoredTextureShader extends Shader<Vec3.F<Vec2.F<Color.RGB<End>>>> {
	public static final ColoredTextureShader INSTANCE = createShader(Id.create("jerraria", "colored_texture"), ColoredTextureShader::new, ColoredTextureShader::new);
	public final Tex<?> texture = this.uni(Tex.tex2d("texture_"));

	public ColoredTextureShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Color.rgb("color")).add(Vec2.f("uv")).add(Vec3.f("pos")), function);
	}

	public ColoredTextureShader(ColoredTextureShader shader, SCopy copy) {
		super(shader, copy);
	}

	public ColoredTextureShader square(Matrix3f mat, Texture texture, float offX, float offY, float width, float height) {
		this.vert().vec3f(mat, offX, offY, 1).uv(texture,0, 0).rgb(0xFFFFFF);
		this.vert().vec3f(mat, offX, offY+height, 1).uv(texture,0, 1).rgb(0xFFFFFF);
		this.vert().vec3f(mat, offX + width, offY, 1).uv(texture,1, 0).rgb(0xFFFFFF);

		this.vert().vec3f(mat, offX+width, offY+height, 1).uv(texture,1, 1).rgb(0xFFFFFF);
		this.vert().vec3f(mat, offX, offY+height, 1).uv(texture,0, 1).rgb(0xFFFFFF);
		this.vert().vec3f(mat, offX + width, offY, 1).uv(texture,1, 0).rgb(0xFFFFFF);
		return this;
	}
}

