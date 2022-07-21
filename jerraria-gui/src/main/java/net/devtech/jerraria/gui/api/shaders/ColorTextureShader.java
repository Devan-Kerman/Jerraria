package net.devtech.jerraria.gui.api.shaders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.batch.BasicShaderKey;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec2;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.MatView;

public class ColorTextureShader extends Shader<Vec3.F<Vec2.F<Color.ARGB<End>>>> {
	public static final ColorTextureShader INSTANCE = create(Id.create("jerraria", "colored_texture_gui"),
		ColorTextureShader::new,
		ColorTextureShader::new
	);

	public final Tex texture = this.uni(Tex.tex2d("texture_"));

	protected ColorTextureShader(VFBuilder<End> builder, Object function) {
		super(builder.add(Color.argb("color")).add(Vec2.f("uv")).add(Vec3.f("pos")), function);
	}

	protected ColorTextureShader(ColorTextureShader shader, SCopy copy) {
		super(shader, copy);
	}

	private static final Map<Integer, BasicShaderKey<ColorTextureShader>> CACHE = new ConcurrentHashMap<>();

	public static BasicShaderKey<ColorTextureShader> keyFor(Texture texture) {
		return CACHE.computeIfAbsent(
			texture.getGlId(),
			id -> BasicShaderKey.key(INSTANCE).withConfig(shader -> shader.texture.tex(id))
		);
	}

	public ColorTextureShader rect(
		MatView mat, Texture texture, float offX, float offY, float width, float height, int color) {
		this.strategy(AutoStrat.QUADS);
		this.vert().vec3f(mat, offX, offY, 1).uv(texture, 0, 0).argb(color);
		this.vert().vec3f(mat, offX, offY + height, 1).uv(texture, 0, 1).argb(color);
		this.vert().vec3f(mat, offX + width, offY + height, 1).uv(texture, 1, 1).argb(color);
		this.vert().vec3f(mat, offX + width, offY, 1).uv(texture, 1, 0).argb(color);
		return this;
	}
}

