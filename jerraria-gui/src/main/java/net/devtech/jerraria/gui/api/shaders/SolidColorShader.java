package net.devtech.jerraria.gui.api.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.batch.StrategyKeys;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.MatView;

public class SolidColorShader extends Shader<Vec3.F<Color.ARGB<End>>> {
	public static final SolidColorShader INSTANCE = create(Id.create("jerraria", "solid_color_gui"), SolidColorShader::new, SolidColorShader::new);
	public static final StrategyKeys<SolidColorShader> KEYS = new StrategyKeys<>(INSTANCE);

	//public final Out out = this.addOutput("oColor", DataType.IMAGE_2D);

	protected SolidColorShader(VFBuilder<End> builder, Object context) {
		super(builder.add(Color.argb("color")).add(Vec3.f("pos")), context);
	}

	public SolidColorShader(Shader<Vec3.F<Color.ARGB<End>>> shader, SCopy method) {
		super(shader, method);
	}

	public void rect(MatView mat, float x, float y, float width, float height, int rgb) {
		final float depth = 1f;
		this.strategy(AutoStrat.QUADS);
		this.vert().vec3f(mat, x, y, depth).argb(rgb);
		this.vert().vec3f(mat, x, y+height, depth).argb(rgb);
		this.vert().vec3f(mat, x+width, y+height, depth).argb(rgb);
		this.vert().vec3f(mat, x+width, y, depth).argb(rgb);
	}
}
