package net.devtech.jerraria.render;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.internal.VFBuilderImpl;

class VertexConsumerImpl<T extends GlValue<?> & GlValue.Attribute> implements VertexConsumer<T> {
	final T built;
	final VertexConsumerGlData data;
	boolean next = false;

	public VertexConsumerImpl(MinecraftShader<T> shader, net.minecraft.client.render.VertexConsumer consumer) {
		GlData data = this.data = new VertexConsumerGlData(shader, consumer);
		this.built = ((VFBuilderImpl<T>) shader.builder).build(data).first();
	}

	@Override
	public T vert() {
		if(this.next) {
			this.data.next();
		} else {
			this.next = true;
		}
		return this.built;
	}

	@Override
	public void flush() {
		if(this.next) {
			this.data.next();
			this.next = false;
		} else {
			this.next = true;
		}
	}
}
