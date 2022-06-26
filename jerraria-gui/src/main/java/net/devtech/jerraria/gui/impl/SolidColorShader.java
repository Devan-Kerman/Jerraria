package net.devtech.jerraria.gui.impl;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.batch.BasicShaderKey;
import net.devtech.jerraria.render.api.batch.ShaderKey;
import net.devtech.jerraria.render.api.batch.StrategyKeys;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Out;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;

public class SolidColorShader extends Shader<Vec3.F<Color.ARGB<End>>> {
	public static final SolidColorShader INSTANCE = create(Id.create("jerraria", "solid_color"), SolidColorShader::new, SolidColorShader::new);
	public static final StrategyKeys<SolidColorShader> KEYS = new StrategyKeys<>(INSTANCE, BasicShaderKey::key);

	public final Out out = this.addOutput("oColor", DataType.IMAGE_2D);

	protected SolidColorShader(VFBuilder<End> builder, Object context) {
		super(builder.add(Color.argb("color")).add(Vec3.f("pos")), context);
	}

	public SolidColorShader(Shader<Vec3.F<Color.ARGB<End>>> shader, SCopy method) {
		super(shader, method);
	}
}
