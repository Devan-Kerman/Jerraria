package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;

public class BlurShader extends Shader<Vec3.F<End>> {
	public static final BlurShader INSTANCE = create(Id.create("jerraria", "impl/blur"), BlurShader::new, BlurShader::new);
	public final Tex tex = this.uni(Tex.tex2d("image"));

	protected BlurShader(VFBuilder<End> builder, Object context) {
		super(builder.add(Vec3.f("pos")), context);
	}

	public BlurShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
