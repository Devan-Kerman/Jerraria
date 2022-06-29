package net.devtech.jerraria.jerraria.tile;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.render.shaders.ChunkTextureShader;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.util.math.Mat2x3f;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.BakingChunk;
import net.devtech.jerraria.world.tile.render.TileRenderer;
import org.jetbrains.annotations.Nullable;

public class BasicTile extends Tile {
	public final Texture texture;

	public BasicTile(String name){
		texture = JerrariaClient.MAIN_ATLAS.getTexture(name);
	}

	public final BasicRenderer render = new BasicRenderer();


	@Override
	public TileRenderer getRenderer(TileVariant variant) {
		return render;
	}

	public class BasicRenderer implements TileRenderer {
		@Override
		public void renderTile(
			BakingChunk source,
			Mat2x3f tileMatrix,
			World localWorld,
			TileVariant variant,
			@Nullable TileData clientTileData,
			int x,
			int y) {
			ChunkTextureShader shader = source.getBatch(ChunkTextureShader.MAIN_ATLAS);
			shader.rect(tileMatrix, BasicTile.this.texture, 0, 0, 1, 1, 0xFFFFFFFF);
		}
	}
}
