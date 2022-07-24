package testmod;

import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.VertexConsumer;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.Instancer;
import net.devtech.jerraria.render.api.instanced.KeyCopying;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.instance.MinecraftInstancer;
import net.devtech.jerraria.render.internal.shaders.BlurResolveShader;
import net.devtech.jerraria.util.math.Mat4f;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class TestBlock extends Block implements BlockEntityProvider {
	public final BlockEntityType<Tile> type = FabricBlockEntityTypeBuilder.create(this::createBlockEntity, this)
		.build();

	public TestBlock(Settings settings) {
		super(settings);

	}

	@Nullable
	@Override
	public Tile createBlockEntity(BlockPos pos, BlockState state) {
		return new Tile(this.type, pos, state);
	}

	static final class Tile extends BlockEntity {
		/**
		 * a more generic version of just an index in an SSBO/UBO array. It automatically updates itself
		 */
		public InstanceKey<TestShader> key;

		public Tile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		@Override
		public void cancelRemoval() {
			super.cancelRemoval();
			assert this.world != null;
			if(this.world.isClient) {
				this.key = CustomRenderLayers.INSTANCER.defaultInstancer.getOrAllocateId(); // allocate instance
			}
		}

		@Override
		public void markRemoved() {
			super.markRemoved();
			if(this.key != null) {
				this.key.invalidate(); // delete instance
				this.key = null;
			}
		}
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
}
