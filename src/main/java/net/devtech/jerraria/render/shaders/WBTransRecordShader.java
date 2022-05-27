package net.devtech.jerraria.render.shaders;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.FrameOut;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.Matrix3f;

public class WBTransRecordShader extends Shader<Vec3.F<Color.ARGB<End>>> {
	public static final WBTransRecordShader INSTANCE = create(
		Id.create("jerraria", "trans/spwb_trans_record"),
		WBTransRecordShader::new,
		WBTransRecordShader::new
	);

	// todo rebind instead of making new frame buffer
	public final FrameOut accum = this.imageOutput("accum");
	public final FrameOut reveal = this.imageOutput("reveal");

	protected WBTransRecordShader(Id id, VFBuilder<End> builder, Object context) {
		super(id, builder.add(Color.argb("color")).add(Vec3.f("pos")), context);
	}

	protected WBTransRecordShader(Shader<Vec3.F<Color.ARGB<End>>> shader, SCopy method) {
		super(shader, method);
	}

	public WBTransRecordShader square(
		Matrix3f mat,
		float offX,
		float offY,
		float width,
		float height,
		float z,
		int color) {
		this.vert().vec3f(mat, offX, offY, z).argb(color);
		this.vert().vec3f(mat, offX, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX, offY + height, z).argb(color);
		this.vert().vec3f(mat, offX + width, offY, z).argb(color);
		return this;
	}
}
