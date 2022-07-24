package net.devtech.jerraria.render.instance;

import net.devtech.jerraria.render.api.instanced.InstanceKey;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public interface BlockEntityInstancer<T, B> {
	void uploadBlockEntityData(
		BlockEntityRendererFactory.Context context,
		InstanceKey<T> key,
		B entity,
		float tickDelta,
		MatrixStack matrices,
		int light,
		int overlay);
}
