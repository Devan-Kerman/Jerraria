package net.devtech.jerraria.jerraria.tile;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.textures.Texture;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.ShaderSource;
import net.devtech.jerraria.world.tile.render.TileRenderer;
import org.jetbrains.annotations.Nullable;

public class TestTile extends Tile {
	public static final Texture WHITE = JerrariaClient.MAIN_ATLAS.getTexture("jerraria/textures/white");
	public static final TestRenderer RENDERER = new TestRenderer();

	@Override
	public TileRenderer getRenderer(TileVariant variant) {
		return RENDERER;
	}

	public static class TestRenderer implements TileRenderer {
		@Override
		public void renderTile(
			ShaderSource source,
			Matrix3f tileMatrix,
			World localWorld,
			TileVariant variant,
			@Nullable TileData clientTileData,
			int x,
			int y) {
			ColoredTextureShader shader = source.computeIfAbsent(ColoredTextureShader.MAIN_ATLAS);
			shader.square(tileMatrix, WHITE, 0, 0, 1, 1);
		}
	}
}
