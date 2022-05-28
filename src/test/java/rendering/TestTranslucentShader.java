package rendering;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.api.types.Vec4;
import net.devtech.jerraria.render.internal.renderhandler.TranslucencyStrategy;
import net.devtech.jerraria.render.internal.renderhandler.translucent.AbstractTranslucencyRenderer;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;

public class TestTranslucentShader extends TranslucentShader<Vec3.F<End>> {
	public static final AbstractTranslucencyRenderer HANDLER = TranslucencyStrategy.createTranslucentRenderer(TranslucencyStrategy.RECOMMENDED);
	public static final TestTranslucentShader INSTANCE = HANDLER.create(
		Id.create("jerraria", "translucency_test"),
		TestTranslucentShader::new,
		TestTranslucentShader::new
	);

	public final Vec4.F<?> color = this.uni(Vec4.f("color"));

	protected TestTranslucentShader(Id id, VFBuilder<End> builder, Object context, TranslucentShaderType type) {
		super(id, builder.add(Vec3.f("pos")), context, type);
	}

	protected TestTranslucentShader(Shader<Vec3.F<End>> shader, SCopy method, TranslucentShaderType type) {
		super(shader, method, type);
	}

	public void square(Matrix3f mat, float offX, float offY, float width, float height, float z) {
		this.vert().vec3f(mat, offX, offY, z);
		this.vert().vec3f(mat, offX, offY + height, z);
		this.vert().vec3f(mat, offX + width, offY, z);
		this.vert().vec3f(mat, offX + width, offY + height, z);
		this.vert().vec3f(mat, offX, offY + height, z);
		this.vert().vec3f(mat, offX + width, offY, z);
	}
}
