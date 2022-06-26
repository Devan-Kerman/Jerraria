package net.devtech.jerraria.render.internal.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;

public class BlurResolveShader extends Shader<Vec3.F<End>> {
	public static final BlurResolveShader INSTANCE = create(Id.create("jerraria", "impl/blur"), BlurResolveShader::new, BlurResolveShader::new);
	public final Tex tex = this.uni(Tex.tex2d("image"));

	protected BlurResolveShader(VFBuilder<End> builder, Object context) {
		super(builder.add(Vec3.f("pos")), context);
	}

	public BlurResolveShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
