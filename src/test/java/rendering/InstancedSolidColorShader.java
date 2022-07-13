package rendering;

import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;

public class InstancedSolidColorShader extends Shader<Vec3.F<End>> {
	public static final InstancedSolidColorShader INSTANCE = create(Id.create("jerraria", "test_instanced"), InstancedSolidColorShader::new, InstancedSolidColorShader::new);

	public final Vec3.F<?>[] offsets = new Vec3.F[32];
	public final Vec3.F<?>[] colors = new Vec3.F[32];

	protected InstancedSolidColorShader(VFBuilder<End> builder, Object function) {
		super(builder.add(Vec3.f("pos")), function);
		this.init();
	}

	protected InstancedSolidColorShader(InstancedSolidColorShader shader, SCopy copy) {
		super(shader, copy);
		this.init();
	}

	protected void init() {
		for(int i = 0; i < this.offsets.length; i++) {
			this.offsets[i] = this.uni(Vec3.f(String.format("offsets[%d]", i)));
		}
		for(int i = 0; i < this.colors.length; i++) {
			this.colors[i] = this.uni(Vec3.f(String.format("colors[%d]", i)));
		}
	}

	/**
	 * draws a rectangle using triangles
	 */
	public void drawRect(Mat mat, float x, float y, float width, float height) {
		this.vert().vec3f(mat, x, y, 1);
		this.vert().vec3f(mat, x+width, y, 1);
		this.vert().vec3f(mat, x, y+height, 1);
		this.vert().vec3f(mat, x+width, y+height, 1);
		this.vert().vec3f(mat, x+width, y, 1);
		this.vert().vec3f(mat, x, y+height, 1);
	}
}
