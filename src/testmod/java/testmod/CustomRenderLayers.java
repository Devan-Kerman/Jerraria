package testmod;

import net.devtech.jerraria.render.RenderLayer;
import net.devtech.jerraria.render.RenderLayers;
import net.devtech.jerraria.render.api.instanced.KeyCopying;
import net.devtech.jerraria.render.instance.MinecraftInstancer;
import net.devtech.jerraria.render.instance.SimpleBlockEntityInstancer;
import net.devtech.jerraria.util.math.Mat4f;

import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class CustomRenderLayers extends RenderLayers {
	public static final RenderLayer<TestShader> RENDER_LAYER = renderLayer(
		TestShader.INSTANCE,
		"custom_render_layer",
		VertexFormats.POSITION,
		VertexFormat.DrawMode.QUADS,
		256,
		false,
		true,
		builder().cull(RenderPhase.DISABLE_CULLING),
		false
	);

	public static final SimpleBlockEntityInstancer<TestShader, TestBlock.Tile> INSTANCER = SimpleBlockEntityInstancer
		.builder(TestBlock.Tile.class, RENDER_LAYER, 1000, entity -> entity.key)
		.model(consumer -> {
			// draw the instance model
			consumer.vert().vec3f(0, 0, 0);
			consumer.vert().vec3f(1, 0, 0);
			consumer.vert().vec3f(1, 1, 0);
			consumer.vert().vec3f(0, 1, 0);
		})
		.data((context, key, entity, tickDelta, matrices, light, overlay) -> {
			// upload per-instance data
			Matrix4f mat = matrices.peek().getPositionMatrix();
			entity.key.ssbo(shader -> shader.blockEntityMats).matN(new Mat4f(mat));
		})
		.relocator((from, to) -> {
			// handle coping (when instances shift around the UBO/SSBO)
			KeyCopying.ssbo(from, to, shader -> shader.blockEntityMats);
		})
		.block((block, context) -> {
			// configure global uniforms
			Mat4f mat = new Mat4f(context.projectionMatrix());
			block.block().projectionMatrix.matN(mat); // set projection matrix
		})
		.build();

	static {
		BlockEntityRendererRegistry.register(Init.BLOCK.type, ctx -> MinecraftInstancer.create(INSTANCER, ctx));
	}
}
