package net.devtech.jerraria.render.shaders;

import java.util.function.Supplier;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Mat3;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec2;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.textures.Atlas;
import net.devtech.jerraria.render.textures.Texture;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.tile.render.ShaderKey;
import net.devtech.jerraria.world.tile.render.ShaderSource;

public class ColoredTextureShader extends Shader<Vec3.F<Vec2.F<Color.RGB<End>>>>
	implements ShaderSource.ShaderConfigurator<ColoredTextureShader> { // vertex attributes
	public static final ColoredTextureShader INSTANCE = createShader(
		Id.create("jerraria", "colored_texture"),
		ColoredTextureShader::new,
		ColoredTextureShader::new
	);

	public static final ShaderKey<ColoredTextureShader> MAIN_ATLAS = keyFor(Id.create("jerraria", "main_atlas"),
		() -> JerrariaClient.MAIN_ATLAS
	);

	static {
		INSTANCE.mat.mat(new Matrix3f());
	}

	public final Tex<?> texture = this.uni(Tex.tex2d("texture_")); // uniforms
	public final Mat3.x3<?> mat = this.uni(Mat3.mat3("mat_"));

	protected ColoredTextureShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Color.rgb("color")).add(Vec2.f("uv")).add(Vec3.f("pos")), function); // vertex attributes
	}

	protected ColoredTextureShader(ColoredTextureShader shader, SCopy copy) {
		super(shader, copy);
	}

	public static ShaderKey<ColoredTextureShader> keyFor(Id name, Texture texture) {
		return new ShaderKey<>(name, (c, s) -> {
			s.texture.atlas(texture);
			s.mat.mat(c);
		}, INSTANCE);
	}

	public static ShaderKey<ColoredTextureShader> keyFor(Id name, Supplier<Atlas> atlasSupplier) {
		return new ShaderKey<>(name, (c, s) -> {
			s.texture.atlas(atlasSupplier.get().asTexture());
			s.mat.mat(c);
		}, INSTANCE);
	}

	public ColoredTextureShader square(
		Matrix3f mat,
		Texture texture,
		float offX,
		float offY,
		float width,
		float height,
		int color) {
		this.vert().vec3f(mat, offX, offY, 1).uv(texture, 0, 0).rgb(color);
		this.vert().vec3f(mat, offX, offY + height, 1).uv(texture, 0, 1).rgb(color);
		this.vert().vec3f(mat, offX + width, offY, 1).uv(texture, 1, 0).rgb(color);

		this.vert().vec3f(mat, offX + width, offY + height, 1).uv(texture, 1, 1).rgb(color);
		this.vert().vec3f(mat, offX, offY + height, 1).uv(texture, 0, 1).rgb(color);
		this.vert().vec3f(mat, offX + width, offY, 1).uv(texture, 1, 0).rgb(color);
		return this;
	}

	@Override
	public void configureUniforms(Matrix3f chunkRenderMatrix, ColoredTextureShader shader) {
		this.mat.mat(chunkRenderMatrix);
	}
}

