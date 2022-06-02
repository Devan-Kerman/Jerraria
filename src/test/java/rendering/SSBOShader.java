package rendering;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderBuffer;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.V;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.api.types.Vec4;
import net.devtech.jerraria.util.Id;

public class SSBOShader extends Shader<Vec3.F<End>> {
	public static final SSBOShader INSTANCE = Shader.create(
		Id.create("jerraria", "ssbo_test"),
		SSBOShader::new,
		SSBOShader::new
	);
	public final ShaderBuffer<Vec4.F<End>> color = this.buffer("colors[%d].color", Vec4::f);
	public final ShaderBuffer<V.F<End>> scale = this.buffer("colors[%d].scale", V::f);
	public final Vec4.F<End> fade = this.uni(Vec4.f("fade"));

	protected SSBOShader(
		Id id, VFBuilder<End> builder, Object context) {
		super(id, builder.add(Vec3.f("pos")), context);
	}

	protected SSBOShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
