package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;

import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Vec3f;

public class Normal<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {

	public static <N extends GlValue<?>> Type<Normal<N>> normal(String name) {
		return normal(name, null);
	}

	public static <N extends GlValue<?>> Type<Normal<N>> normal(String name, String groupName) {
		return simple(Normal::new, DataType.NORMALIZED_F8_VEC3, name, groupName);
	}

	protected Normal(GlData data, GlValue next, String name) {
		super(data, next, name);
	}

	public N normal(byte x, byte y, byte z) {
		this.data.element(this.element).b(x).b(y).b(z);
		return this.getNext();
	}

	public N normal(float x, float y, float z) {
		return this.normal(BufferVertexConsumer.packByte(x),
			BufferVertexConsumer.packByte(y),
			BufferVertexConsumer.packByte(z)
		);
	}

	public N normal(Matrix3f mat, float x, float y, float z) {
		Vec3f vec3f = new Vec3f(x, y, z);
		vec3f.transform(mat);
		return this.normal(vec3f.getX(), vec3f.getY(), vec3f.getZ());
	}

	public N normal(MatrixStack.Entry entry, float x, float y, float z) {
		return this.normal(entry.getNormalMatrix(), x, y, z);
	}

	public N normal(MatrixStack stack, float x, float y, float z) {
		return this.normal(stack.peek(), x, y, z);
	}
}
