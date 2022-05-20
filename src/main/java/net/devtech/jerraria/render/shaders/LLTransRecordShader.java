package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.ImageFormat;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.V;
import net.devtech.jerraria.render.api.types.Vec2;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;

public class LLTransRecordShader extends Shader<Vec3.F<Color.ARGB<End>>> { // vertex attributes
	public static final LLTransRecordShader INSTANCE = createShader(
		Id.create("jerraria", "ll_trans_record"),
		LLTransRecordShader::new,
		LLTransRecordShader::new
	);

	public final V.UI<?> counter = this.uni(V.atomic_ui("counter"));
	public final Tex<?> imgListHead = this.uni(Tex.img("imgListHead", DataType.UINT_IMAGE_2D, ImageFormat.R32UI));
	public final Tex<?> translucencyBuffer = this.uni(Tex.img("translucencyBuffer", DataType.UINT_IMAGE_BUFFER, ImageFormat.RGBA32UI));

	protected LLTransRecordShader(Id id, VFBuilder<End> builder, Object function) {
		super(id, builder.add(Color.argb("color")).add(Vec3.f("pos")), function); // vertex attributes
	}

	protected LLTransRecordShader(LLTransRecordShader shader, SCopy copy) {
		super(shader, copy);
	}

	public LLTransRecordShader square(
		Matrix3f mat,
		float offX,
		float offY,
		float width,
		float height,
		float z,
		int color) {
		this.vert().vec3f(mat, offX, offY, z).argb(color);
		this.vert().vec3f(mat, offX, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY, z).argb(color);
		return this;
	}
}

