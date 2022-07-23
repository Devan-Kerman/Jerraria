package testmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.Instancer;
import net.devtech.jerraria.util.math.Mat4f;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class TestBlock extends Block implements BlockEntityProvider {
	public final BlockEntityType<Tile> type = FabricBlockEntityTypeBuilder.create(this::createBlockEntity, this)
		.build();

	public TestBlock(Settings settings) {
		super(settings);
		BlockEntityRendererRegistry.register(this.type, Renderer::new);
	}

	@Nullable
	@Override
	public Tile createBlockEntity(BlockPos pos, BlockState state) {
		return new Tile(this.type, pos, state);
	}

	static final class Tile extends BlockEntity {
		InstanceKey<TestShader> key;

		public Tile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		@Override
		public void cancelRemoval() {
			super.cancelRemoval();
			assert this.world != null;
			if(this.world.isClient) {
				this.key = Renderer.SHADER_INSTANCER.getOrAllocateId();
			}
		}

		@Override
		public void markRemoved() {
			super.markRemoved();
			if(this.key != null) {
				this.key.invalidate();
				this.key = null;
			}
		}
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	record Renderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<Tile> {
		static final Instancer<TestShader> SHADER_INSTANCER = Instancer.simple(TestShader::copy, TestShader.INSTANCE, 1000);
		static {
			TestShader instance = TestShader.INSTANCE;
			instance.strategy(AutoStrat.QUADS);
			instance.vert().vec3f(0, 0, 0);
			instance.vert().vec3f(1, 0, 0);
			instance.vert().vec3f(1, 1, 0);
			instance.vert().vec3f(0, 1, 0);
		}

		@Override
		public void render(
			Tile entity,
			float tickDelta,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay) {
			Matrix4f mat = matrices.peek().getPositionMatrix();
			entity.key.ssbo(shader -> shader.blockEntityMats).matN(new Mat4f(mat));
			for(Instancer.Block<TestShader> block : SHADER_INSTANCER.compactAndGetBlocks()) {
				block.block().projectionMatrix.matN(new Mat4f(RenderSystem.getProjectionMatrix()));
				block.block().modelViewMatrix.matN(new Mat4f(RenderSystem.getModelViewMatrix()));
				block.block().drawInstancedKeep(block.instances());
			}
		}
	}
}
