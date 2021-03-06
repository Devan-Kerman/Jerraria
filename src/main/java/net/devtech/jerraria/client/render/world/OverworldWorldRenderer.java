package net.devtech.jerraria.client.render.world;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.client.WorldRenderer;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.client.ClientWorld;

public class OverworldWorldRenderer extends WorldRenderer {
	private static final int[] COLORS = {0xFFAACCFF, 0xFFB0CFFF, 0xFFBFDDFF, 0xFFC0E0FF, 0xFFCCEEFF, 0xFFDDEEFF, 0xFFEEEEFF};
	final CloudVoronoi voronoi = new CloudVoronoi(10, 34);
	// todo cloud wander offset for wind direction

	public OverworldWorldRenderer(ClientWorld world) {
		super(world);
	}

	@Override
	protected void renderBackground(
		Mat cartToAwt,
		Entity player,
		int windowFromX,
		int windowFromY,
		int windowToX,
		int windowToY) {
		SolidColorShader shader = SolidColorShader.INSTANCE;
		float width = ((float) JerrariaClient.windowWidth()) / JerrariaClient.windowHeight();
		shader.strategy(AutoStrat.QUADS);
		float inc = 1f/COLORS.length;
		for(int i = 0; i < COLORS.length; i++) {
			float off = i * inc;
			int color = COLORS[i];
			shader.rect(cartToAwt, 0, 0, inc, inc, color);
		}
		//shader.vert().argb(0xFFAACCFF).vec3f(cartToAwt,0, 0, 1);
		//shader.vert().argb(0xFFEEEEFF).vec3f(cartToAwt,0, 1, 1);
		//shader.vert().argb(0xFFEEEFFF).vec3f(cartToAwt, width, 1, 1);
		//shader.vert().argb(0xFFAADDFF).vec3f(cartToAwt, width, 0, 1);

		int off = player.getBlockX()/32;
		Mat cloud = cartToAwt.copy().scale(1 / 100f, 1 / 100f);
		for(int x = 0; x < 100*width; x++) {
			for(int y = 0; y < 50; y++) {
				if(y%10 < 5) {
					int generate = this.voronoi.generate(x+off, y, 10);
					if(generate < 2) {
						shader.rect(cloud, x, y, 1, 1, 0xFFFFFFFF);
					}
				}
			}
		}
		shader.draw();
	}
}
