package net.devtech.jerraria.client.overworld;

import net.devtech.jerraria.client.WorldRenderer;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.client.ClientWorld;

public class OverworldWorldRenderer extends WorldRenderer {
	public OverworldWorldRenderer(ClientWorld world) {
		super(world);
	}

	@Override
	protected void renderBackground(Matrix3f cartToAwt, Entity player, int blockScreenWidth, int blockScreenHeight) {
		SolidColorShader shader = SolidColorShader.INSTANCE;
	}
}
