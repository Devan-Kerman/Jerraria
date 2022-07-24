package net.devtech.jerraria.render.instance;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import net.devtech.jerraria.render.DefaultVertexConsumer;
import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.RenderLayer;
import net.devtech.jerraria.render.VertexConsumer;
import net.devtech.jerraria.render.VertexConsumerProvider;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.InstanceRelocator;
import net.devtech.jerraria.render.api.instanced.Instancer;

import net.minecraft.block.entity.BlockEntity;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public abstract class SimpleMinecraftInstancer<T extends MinecraftShader<?>, B> extends MinecraftInstancer<T, B> {
	public final Instancer<T> defaultInstancer;
	final RenderLayer<T> renderLayer;
	final int maxInstances;
	final List<InstanceRelocator<T>> copiers;
	final Function<B, InstanceKey<T>> keyExtracter;
	final List<Consumer<VertexConsumer<?>>> modelDrawer;
	final List<BlockConfigurator<T>> configurators;

	public SimpleMinecraftInstancer(
		RenderLayer<T> renderLayer,
		int instances,
		List<InstanceRelocator<T>> copiers,
		Function<B, InstanceKey<T>> extracter,
		List<Consumer<VertexConsumer<?>>> modelDrawer,
		List<BlockConfigurator<T>> configurators) {
		this.renderLayer = renderLayer;
		this.maxInstances = instances;
		this.copiers = copiers;
		this.keyExtracter = extracter;
		this.modelDrawer = modelDrawer;
		this.configurators = configurators;
		T shader = Shader.copy(renderLayer.shader, SCopy.PRESERVE_UNIFORMS);
		for(Consumer<VertexConsumer<?>> consumer : modelDrawer) {
			//noinspection unchecked
			consumer.accept(new DefaultVertexConsumer(shader));
		}
		this.defaultInstancer = Instancer.simple((from, to) -> {
			for(InstanceRelocator<T> copier : this.copiers) {
				copier.copy(from, to);
			}
		}, shader, this.maxInstances);
	}

	@Override
	public void acceptSingleInstance(
		WorldRenderContext worldRenderContext, Instancer<T> instancer, Instancer.Block<T> element, InstanceKey<T> id) {
		for(BlockConfigurator<T> configurator : this.configurators) {
			configurator.configure(element, worldRenderContext);
		}
	}

	@Override
	protected <V extends GlValue<?> & GlValue.Attribute> RenderLayer<? extends MinecraftShader<V>> wrap(
		Instancer.Block<T> block, RenderLayer<? extends MinecraftShader<V>> renderLayer) {
		return (RenderLayer<? extends MinecraftShader<V>>) renderLayer.withShader(block.block());
	}

	@Override
	protected Instancer<T> newInstancer() {
		return Instancer.simple((from, to) -> {
			for(InstanceRelocator<T> copier : this.copiers) {
				copier.copy(from, to);
			}
		}, this.renderLayer.shader, this.maxInstances);
	}

	@Override
	protected InstanceKey<T> getDefaultKey(B instance) {
		return this.keyExtracter.apply(instance);
	}

	@Override
	protected void drawModel(VertexConsumerProvider provider) {
		VertexConsumer consumer = provider.getConsumer((RenderLayer) this.renderLayer);
		for(Consumer<VertexConsumer<?>> modelDrawer : this.modelDrawer) {
			modelDrawer.accept(consumer);
		}
		consumer.flush();
	}

	@Override
	protected void drawInstanced(WorldRenderContext context) {
		this.renderLayer.layer.startDrawing();
		for(Instancer.Block<T> block : this.defaultInstancer.compactAndGetBlocks()) {
			for(BlockConfigurator<T> configurator : this.configurators) {
				configurator.configure(block, context);
			}
			block.block().drawInstancedKeep(block.instances());
		}
		this.renderLayer.layer.endDrawing();
	}

	public interface BlockConfigurator<T2> {
		void configure(Instancer.Block<T2> block, WorldRenderContext context);
	}

	public static abstract class Builder<V extends GlValue<?> & GlValue.Attribute, T extends MinecraftShader<V>, B, C extends Builder<V, T, B, C>> {
		final RenderLayer<T> renderLayer;
		final int maxInstances;
		final Function<B, InstanceKey<T>> keyExtracter;
		final List<InstanceRelocator<T>> relocators;
		final List<Consumer<VertexConsumer<V>>> modelDrawer;
		final List<BlockConfigurator<T>> configurators;

		public Builder(
			RenderLayer<T> layer,
			int instances,
			Function<B, InstanceKey<T>> extracter,
			List<InstanceRelocator<T>> relocators,
			List<Consumer<VertexConsumer<V>>> drawer,
			List<BlockConfigurator<T>> configurators) {
			this.renderLayer = layer;
			this.maxInstances = instances;
			this.keyExtracter = extracter;
			this.relocators = relocators;
			this.modelDrawer = drawer;
			this.configurators = configurators;
		}

		public Builder(RenderLayer<T> layer, int instances, Function<B, InstanceKey<T>> extracter) {
			this(layer, instances, extracter, List.of(), List.of(), List.of());
		}

		protected abstract C create(RenderLayer<T> layer,
			int instances,
			Function<B, InstanceKey<T>> extracter,
			List<InstanceRelocator<T>> relocators,
			List<Consumer<VertexConsumer<V>>> drawer,
			List<BlockConfigurator<T>> configurators);

		public C relocator(InstanceRelocator<T> relocator) {
			return create(this.renderLayer,
				this.maxInstances,
				this.keyExtracter,
				ImmutableList.<InstanceRelocator<T>>builder().addAll(this.relocators).add(relocator).build(),
				this.modelDrawer,
				this.configurators
			);
		}

		public C model(Consumer<VertexConsumer<V>> drawer) {
			return create(this.renderLayer,
				this.maxInstances,
				this.keyExtracter,
				this.relocators,
				ImmutableList.<Consumer<VertexConsumer<V>>>builder().addAll(this.modelDrawer).add(drawer).build(),
				this.configurators
			);
		}

		public C block(BlockConfigurator<T> configurator) {
			return create(this.renderLayer,
				this.maxInstances,
				this.keyExtracter,
				this.relocators,
				this.modelDrawer,
				ImmutableList.<SimpleBlockEntityInstancer.BlockConfigurator<T>>builder()
					.addAll(this.configurators)
					.add(configurator)
					.build()
			);
		}
	}
}
