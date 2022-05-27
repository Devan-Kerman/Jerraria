package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;

public class WBTransResolveShader extends Shader<Vec3.F<End>> {
	public static final WBTransResolveShader INSTANCE = create(
		Id.create("jerraria", "impl/wb_trans_resolve"),
		WBTransResolveShader::new,
		WBTransResolveShader::new
	);

	public final Tex accum = this.uni(Tex.tex2d("accum"));
	public final Tex reveal = this.uni(Tex.tex2d("reveal"));

	protected WBTransResolveShader(
		Id id, VFBuilder<End> builder, Object context) {
		super(id, builder.add(Vec3.f("pos")), context);
	}

	protected WBTransResolveShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
