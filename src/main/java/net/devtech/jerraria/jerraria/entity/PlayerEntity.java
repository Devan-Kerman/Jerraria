package net.devtech.jerraria.jerraria.entity;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.textures.Texture;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.util.math.SimpleShape;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.BaseEntity;
import net.devtech.jerraria.world.entity.render.AbstractEntityRenderer;
import net.devtech.jerraria.world.entity.render.EntityRenderer;

public class PlayerEntity extends BaseEntity {
	public static final SimpleShape PLAYER_SHAPE = new SimpleShape.Rectangle(40, 40);
	double packetX = Double.POSITIVE_INFINITY, packetY;

	public PlayerEntity(Type<?> type) {
		super(type, PLAYER_SHAPE);
	}

	protected PlayerEntity(Type<?> type, JCElement<?> element, World world, double x, double y) {
		super(type, element, world, x, y, PLAYER_SHAPE);
	}

	@Override
	protected void tick() {
		if(packetX != Double.POSITIVE_INFINITY) {
			this.updatePosition(this.getWorld(), this.packetX, this.packetY);
		}
	}

	/**
	 * @return players will have to be stored separately
	 */
	@Override
	public boolean doesSaveInChunk() {
		return false;
	}

	@Override
	protected EntityRenderer createRenderer() {
		return new Renderer();
	}

	public class Renderer extends AbstractEntityRenderer<PlayerEntity> {
		final Texture texture = JerrariaClient.MAIN_ATLAS.getTexture("jerraria/textures/basic_player");

		public Renderer() {
			super(PlayerEntity.this);
		}

		@Override
		public void renderEntity(Matrix3f matrix) {
			ColoredTextureShader shader = ColoredTextureShader.INSTANCE;
			shader.texture.atlas(texture);
			shader.square(matrix, texture, 0, 0, 8, 16);
		}
	}
}
