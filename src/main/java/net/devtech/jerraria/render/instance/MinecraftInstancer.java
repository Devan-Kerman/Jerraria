package net.devtech.jerraria.render.instance;

import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.RenderLayer;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.Instancer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public abstract class MinecraftInstancer<T, B> {
	public abstract void acceptSingleInstance(
		WorldRenderContext worldRenderContext, Instancer<T> instancer, Instancer.Block<T> element, InstanceKey<T> id);

	/**
	 * Normally, when rendering to the world we can use our own renderer, however sometimes mods will cache or bake
	 * BERs
	 * for one reason or another. This function is designed to wrap the render layer for an individual block.
	 *
	 * @see RenderLayer#withShader(MinecraftShader)
	 */
	protected abstract <V extends GlValue<?> & GlValue.Attribute> RenderLayer<? extends MinecraftShader<V>> wrap(
		Instancer.Block<T> block,
		RenderLayer<? extends MinecraftShader<V>> renderLayer);

	/**
	 * @return A new instancer
	 */
	protected abstract Instancer<T> newInstancer();

	protected abstract InstanceKey<T> getDefaultKey(B instance);

	/**
	 * @see RenderLayer#from(RenderLayer, VertexConsumerProvider)
	 */
	protected abstract void drawModel(net.devtech.jerraria.render.VertexConsumerProvider provider);

	protected abstract void drawInstanced(WorldRenderContext context1);

	public static <T, M extends MinecraftInstancer<T, B> & BlockEntityInstancer<T, B>, B extends BlockEntity> BlockEntityRenderer<B> create(
		M instancer,
		BlockEntityRendererFactory.Context context) {
		return new InstancedBlockEntityRenderer<>(context, instancer);
	}
}
