package net.devtech.jerraria.render.shaders;

import java.util.function.Supplier;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Mat3;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec2;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.textures.Atlas;
import net.devtech.jerraria.render.textures.Texture;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.tile.render.ChunkShaderKey;

public class ColoredTextureShader extends Shader<Vec3.F<Vec2.F<Color.ARGB<End>>>> { // vertex attributes
	public static final ColoredTextureShader INSTANCE = create(Id.create("jerraria", "colored_texture"),
		ColoredTextureShader::new,
		ColoredTextureShader::new
	);

	public static final ChunkShaderKey<ColoredTextureShader> MAIN_ATLAS = keyFor(() -> JerrariaClient.MAIN_ATLAS);

	static {
		INSTANCE.mat.identity();
	}

	public final Tex texture = this.uni(Tex.tex2d("texture_")); // uniforms

	/**
	 * Chunk translation matrix
	 */
	public final Mat3.x3<?> mat = this.uni(Mat3.mat3("mat_"));

	protected ColoredTextureShader(VFBuilder<End> builder, Object function) {
		super(builder.add(Color.argb("color")).add(Vec2.f("uv")).add(Vec3.f("pos")), function); // vertex
		// attributes
	}

	protected ColoredTextureShader(ColoredTextureShader shader, SCopy copy) {
		super(shader, copy);
	}

	public static ChunkShaderKey<ColoredTextureShader> keyFor(Texture texture) {
		return new ChunkShaderKey<>((c, s) -> {
			s.texture.atlas(texture);
			s.mat.mat(c);
		}, INSTANCE);
	}

	public static ChunkShaderKey<ColoredTextureShader> keyFor(Supplier<Atlas> atlasSupplier) {
		return new ChunkShaderKey<>((c, s) -> {
			s.texture.atlas(atlasSupplier.get().asTexture());
			s.mat.mat(c);
		}, INSTANCE);
	}

	public ColoredTextureShader rect(
		Matrix3f mat,
		Texture texture,
		float offX,
		float offY,
		float width,
		float height,
		int color) {
		this.strategy(AutoStrat.QUADS);
		this.vert().vec3f(mat, offX, offY, 1).uv(texture, 0, 0).argb(color);
		this.vert().vec3f(mat, offX, offY + height, 1).uv(texture, 0, 1).argb(color);
		this.vert().vec3f(mat, offX + width, offY + height, 1).uv(texture, 1, 1).argb(color);
		this.vert().vec3f(mat, offX + width, offY, 1).uv(texture, 1, 0).argb(color);
		return this;
	}
}

