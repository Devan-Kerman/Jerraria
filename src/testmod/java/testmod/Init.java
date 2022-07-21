package testmod;

import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.util.math.Mat4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;

public class Init implements ClientModInitializer {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	@Override
	public void onInitializeClient() {
		ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((a, b) -> MinecraftClient.getInstance()
			.setScreenAndRender(new Screen(Text.literal("test")) {
				@Override
				public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
					super.render(matrices, mouseX, mouseY, delta);
					try(GLStateBuilder.builder().depthTest(false).faceCulling(false).apply().self) {
						SolidColorShader instance = SolidColorShader.INSTANCE;
						instance.rect(new Mat4f(), -1, -1, 2, 2, 0xFFFFFFFF);
						instance.draw();
					}
				}
			}));
	}
}
