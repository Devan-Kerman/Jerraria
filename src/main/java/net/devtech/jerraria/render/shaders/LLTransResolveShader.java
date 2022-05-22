package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.ImageFormat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;

public class LLTransResolveShader extends Shader<Vec3.F<End>> {
	public static final LLTransResolveShader INSTANCE = createShader(
		Id.create("jerraria", "trans/ll_trans_resolve"),
		LLTransResolveShader::new,
		LLTransResolveShader::new
	);

	public final Tex imgListHead = this.uni(Tex.img("imgListHead", DataType.UINT_IMAGE_2D, ImageFormat.R32UI));
	public final Tex translucencyBuffer = this.uni(Tex.img("translucencyBuffer", DataType.UINT_IMAGE_BUFFER, ImageFormat.RGBA32UI));

	protected LLTransResolveShader(Id id, VFBuilder<End> builder, Object context) {
		super(id, builder.add(Vec3.f("pos")), context);
	}

	public LLTransResolveShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
