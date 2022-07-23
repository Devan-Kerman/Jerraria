package testmod;

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

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
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
		public Tile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	record Renderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<Tile> {
		@Override
		public void render(
			Tile entity,
			float tickDelta,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay) {
			VertexConsumer buffer = vertexConsumers.getBuffer(CustomRenderLayers.RENDER_LAYER);
			Matrix4f mat = matrices.peek().getPositionMatrix();
			buffer.vertex(mat, 0, 0, 0).next();
			buffer.vertex(mat, 1, 0, 0).next();
			buffer.vertex(mat, 1, 1, 0).next();
			buffer.vertex(mat, 0, 1, 0).next();
		}
	}
}
