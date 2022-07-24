package net.devtech.jerraria.render.instance;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.RenderLayer;
import net.devtech.jerraria.render.VertexConsumer;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.InstanceRelocator;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class SimpleBlockEntityInstancer<T extends MinecraftShader<?>, B> extends SimpleMinecraftInstancer<T, B>
	implements BlockEntityInstancer<T, B> {
	final List<InstanceUploader<T, B>> uploaders;

	public static <V extends GlValue<?> & GlValue.Attribute, T extends MinecraftShader<V>, B> Builder<V, T, B> builder(
		Class<B> type, RenderLayer<T> layer, int instances, Function<B, InstanceKey<T>> extracter) {
		return new Builder<>(layer, instances, extracter);
	}

	public SimpleBlockEntityInstancer(
		RenderLayer<T> renderLayer,
		int instances,
		List<InstanceRelocator<T>> copiers,
		Function<B, InstanceKey<T>> extracter,
		List<Consumer<VertexConsumer<?>>> modelDrawer,
		List<InstanceUploader<T, B>> uploaders,
		List<BlockConfigurator<T>> configurators) {
		super(renderLayer, instances, copiers, extracter, modelDrawer, configurators);
		this.uploaders = uploaders;
	}

	@Override
	public void uploadBlockEntityData(
		BlockEntityRendererFactory.Context context,
		InstanceKey<T> key,
		B entity,
		float tickDelta,
		MatrixStack matrices,
		int light,
		int overlay) {
		for(InstanceUploader<T, B> uploader : this.uploaders) {
			uploader.upload(context, key, entity, tickDelta, matrices, light, overlay);
		}
	}

	public interface InstanceUploader<T extends MinecraftShader<?>, B> {
		void upload(
			BlockEntityRendererFactory.Context context,
			InstanceKey<T> key,
			B entity,
			float tickDelta,
			MatrixStack matrices,
			int light,
			int overlay);
	}

	public static class Builder<V extends GlValue<?> & GlValue.Attribute, T extends MinecraftShader<V>, B>
		extends SimpleMinecraftInstancer.Builder<V, T, B, Builder<V, T, B>> {
		final List<InstanceUploader<T, B>> uploaders;

		public Builder(
			RenderLayer<T> layer,
			int instances,
			Function<B, InstanceKey<T>> extracter,
			List<InstanceRelocator<T>> relocators,
			List<Consumer<VertexConsumer<V>>> drawer,
			List<BlockConfigurator<T>> configurators,
			List<InstanceUploader<T, B>> uploaders) {
			super(layer, instances, extracter, relocators, drawer, configurators);
			this.uploaders = uploaders;
		}

		public Builder(RenderLayer<T> layer, int instances, Function<B, InstanceKey<T>> extracter) {
			super(layer, instances, extracter);
			this.uploaders = List.of();
		}

		@Override
		protected Builder<V, T, B> create(
			RenderLayer<T> layer,
			int instances,
			Function<B, InstanceKey<T>> extracter,
			List<InstanceRelocator<T>> relocators,
			List<Consumer<VertexConsumer<V>>> drawer,
			List<BlockConfigurator<T>> configurators) {
			return new Builder<>(layer, instances, extracter, relocators, drawer, configurators, this.uploaders);
		}

		public Builder<V, T, B> data(SimpleBlockEntityInstancer.InstanceUploader<T, B> uploader) {
			return new Builder<>(
				this.renderLayer,
				this.maxInstances,
				this.keyExtracter,
				this.relocators,
				this.modelDrawer,
				this.configurators,
				ImmutableList.<InstanceUploader<T, B>>builder().addAll(this.uploaders).add(uploader).build()
			);
		}

		@SuppressWarnings("unchecked")
		public <B2> Builder<V, T, B2> dataTyped(
			Class<B2> type, SimpleBlockEntityInstancer.InstanceUploader<T, B2> uploader) {
			return new Builder<>(
				this.renderLayer,
				this.maxInstances,
				this.keyExtracter,
				this.relocators,
				this.modelDrawer,
				this.configurators,
				ImmutableList.<SimpleBlockEntityInstancer.InstanceUploader<T, B2>>builder()
					.addAll((Iterable) this.uploaders)
					.add(uploader)
					.build()
			);
		}

		public SimpleBlockEntityInstancer<T, B> build() {
			//noinspection unchecked,rawtypes
			return new SimpleBlockEntityInstancer<T, B>(
				this.renderLayer,
				this.maxInstances,
				this.relocators,
				this.keyExtracter,
				(List) this.modelDrawer,
				this.uploaders,
				this.configurators
			);
		}
	}
}
