package net.devtech.jerraria.render.instance;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Iterables;
import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.RenderLayer;
import net.devtech.jerraria.render.VertexConsumer;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.Instancer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class InstancedBlockEntityRenderer<T, B extends BlockEntity, M extends MinecraftInstancer<T, B> & BlockEntityInstancer<T, B>> implements BlockEntityRenderer<B> {
	static final Set<MinecraftInstancer<?, ?>> RENDERERS = Collections.newSetFromMap(new ConcurrentHashMap<>());
	static volatile WorldRenderContext worldRenderContext;
	static {
		WorldRenderEvents.START.register(context -> {
			worldRenderContext = context;
			RENDERERS.clear();
		});
		WorldRenderEvents.LAST.register(context1 -> {
			for(MinecraftInstancer<?, ?> renderer : RENDERERS) {
				renderer.drawInstanced(context1);
			}
		});
		WorldRenderEvents.END.register(context -> {
			RENDERERS.clear();
			worldRenderContext = null;
		});
	}

	final BlockEntityRendererFactory.Context context;
	final M type;

	public InstancedBlockEntityRenderer(BlockEntityRendererFactory.Context context, M type) {
		this.context = context;
		this.type = type;
	}

	@Override
	public void render(
		B entity,
		float tickDelta,
		MatrixStack matrices,
		VertexConsumerProvider vertexConsumers,
		int light,
		int overlay) {
		if(vertexConsumers instanceof VertexConsumerProvider.Immediate) {
			RENDERERS.add(this.type);
			this.type.uploadBlockEntityData(this.context, this.type.getDefaultKey(entity), entity, tickDelta, matrices, light, overlay);
		} else {
			Instancer<T> instancer = this.type.newInstancer();
			InstanceKey<T> id = instancer.getOrAllocateId();
			Instancer.Block<T> element = Iterables.getOnlyElement(instancer.compactAndGetBlocks());
			this.type.drawModel(new net.devtech.jerraria.render.VertexConsumerProvider() {
				@Override
				public <X extends GlValue<?> & GlValue.Attribute> VertexConsumer<X> getConsumer(RenderLayer<? extends MinecraftShader<X>> renderLayer) {
					return RenderLayer.from(InstancedBlockEntityRenderer.this.type.wrap(element, renderLayer), vertexConsumers);
				}
			});
			this.type.uploadBlockEntityData(this.context, id, entity, tickDelta, matrices, light, overlay);
			this.type.acceptSingleInstance(worldRenderContext, instancer, element, id);
		}
	}
}
