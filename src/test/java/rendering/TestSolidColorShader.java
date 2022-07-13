package rendering;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.ImageFormat;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;

public class TestSolidColorShader extends Shader<Vec3.F<End>> {
	public static final TestSolidColorShader INSTANCE = create(Id.create("jerraria", "test_solid_color"), TestSolidColorShader::new, TestSolidColorShader::new);

	public final Tex imgListHead = this.uni(Tex.img("imgListHead", DataType.UINT_IMAGE_2D, ImageFormat.R32UI));

	protected TestSolidColorShader(VFBuilder<End> builder, Object function) {
		super(builder.add(Vec3.f("pos")), function);
	}

	protected TestSolidColorShader(TestSolidColorShader shader, SCopy copy) {
		super(shader, copy);
	}

	/**
	 * draws a rectangle using triangles
	 */
	public void drawRect(Mat mat, float x, float y, float width, float height, int rgb) {
		this.vert().vec3f(mat, x, y, 1);
		this.vert().vec3f(mat, x+width, y, 1);
		this.vert().vec3f(mat, x, y+height, 1);

		this.vert().vec3f(mat, x+width, y+height, 1);
		this.vert().vec3f(mat, x+width, y, 1);
		this.vert().vec3f(mat, x, y+height, 1);
	}
}
