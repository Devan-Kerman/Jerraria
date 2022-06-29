package net.devtech.jerraria.jerraria.entity;

import java.util.List;

import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.math.Mat2x3f;
import net.devtech.jerraria.util.math.Rectangle;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.BaseEntity;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.entity.render.AbstractEntityRenderer;
import net.devtech.jerraria.world.entity.render.EntityRenderer;

public class PlayerEntity extends BaseEntity {
	public static final List<Rectangle> PLAYER_SHAPE = List.of(new Rectangle(40, 40));
	double packetX = Double.POSITIVE_INFINITY, packetY;

	public PlayerEntity(Type<?> type) {
		super(type, PLAYER_SHAPE);
	}

	protected PlayerEntity(Type<?> type, JCElement<?> element, World world, double x, double y) {
		super(type, element, world, x, y, PLAYER_SHAPE);
	}

	@Override
	protected void tick() {
		super.tick();
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

	@Override
	public boolean doesIntersect(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
		if(type == EntitySearchType.Standard.RENDERING) {
			return true;
		}
		return super.doesIntersect(type, fromX, fromY, toX, toY);
	}

	public class Renderer extends AbstractEntityRenderer<PlayerEntity> {
		//final Texture texture = JerrariaClient.MAIN_ATLAS.getTexture("jerraria/textures/basic_player");

		public Renderer() {
			super(PlayerEntity.this);
		}

		@Override
		public void renderEntity(
			Entity entity,
			Mat2x3f matrix,
			int windowFromX,
			int windowFromY,
			int windowToX,
			int windowToY) {
			SolidColorShader shader = SolidColorShader.INSTANCE;
			shader.rect(matrix, 10, 10, 10, 10, 0xFFFFFFFF);
			shader.draw();
		}
	}
}
