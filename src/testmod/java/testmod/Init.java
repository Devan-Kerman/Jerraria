package testmod;

import java.util.Objects;

import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.api.types.Mat4;
import net.devtech.jerraria.render.internal.shaders.BlurResolveShader;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat4f;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class Init implements ClientModInitializer {
	public static final TestBlock BLOCK = new TestBlock(AbstractBlock.Settings.copy(Blocks.STONE));
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	@Override
	public void onInitializeClient() {
		Identifier id = new Identifier("aaaaaa", "aaaaa");
		Registry.register(Registry.BLOCK, id, BLOCK);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id, BLOCK.type);
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			Objects.requireNonNull(CustomRenderLayers.RENDER_LAYER);
			Objects.requireNonNull(BlurResolveShader.INSTANCE);
		});

	}
}
