package rendering;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderBuffer;
import net.devtech.jerraria.render.api.Struct;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.V;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.api.types.Vec4;
import net.devtech.jerraria.util.Id;

public class SSBOShader extends Shader<Vec3.F<End>> {
	public static final Struct.TypeFactory<Instance> FACTORY = Struct.factory(Instance::new);
	public static final SSBOShader INSTANCE = Shader.create(
		Id.create("jerraria", "ssbo_test"),
		SSBOShader::new,
		SSBOShader::new
	);

	public final ShaderBuffer<Instance> instances = this.buffer("colors[%d]", FACTORY::named);
	public final Vec4.F<End> fade = this.uni(Vec4.f("fade"));

	static class Instance extends Struct {
		public final Vec4.F<End> color = this.field(Vec4.f(this.name + ".color"));
		public final V.F<End> scale = this.field(V.f(this.name + ".scale"));

		protected Instance(GlData data, GlValue next, String name) {
			super(data, next, name);
		}
	}

	protected SSBOShader(VFBuilder<End> builder, Object context) {
		super(builder.add(Vec3.f("pos")), context);
	}

	protected SSBOShader(Shader<Vec3.F<End>> shader, SCopy method) {
		super(shader, method);
	}
}
