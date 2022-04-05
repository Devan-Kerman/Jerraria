package net.devtech.jerraria.world.entity;

import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.math.SimpleShape;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.render.EntityRenderer;

import java.util.Objects;

public abstract class Entity extends BaseEntity {
	SimpleShape bounds;
	double dx, dy;
	EntityRenderer renderer;

	public Entity(Type<?> type, SimpleShape bounds) {
		super(type);
		this.bounds = bounds;
	}

	protected Entity(Type<?> type, JCElement<?> element, World world, double x, double y, SimpleShape bounds) {
		super(type, element, world, x, y);
		this.bounds = bounds;
	}

	protected abstract EntityRenderer createRenderer();

	public EntityRenderer getRenderer() {
		EntityRenderer renderer = this.renderer;
		if(renderer == null) {
			this.renderer = renderer = Objects.requireNonNull(this.createRenderer(), "Cannot have null entity renderer!");
		}
		return renderer;
	}

	public void setVelocity(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}

	public double getDx() {
		return this.dx;
	}

	public double getDy() {
		return this.dy;
	}

	@Override
	public boolean isEnclosed(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
		double x = this.getX(), y = this.getY();
		return this.bounds.isEnclosed(fromX - x, fromY - y, toX - x, toY - y);
	}

	@Override
	public boolean doesIntersect(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
		double x = this.getX(), y = this.getY();
		return this.bounds.doesIntersect(fromX - x, fromY - y, toX - x, toY - y);
	}

	protected SimpleShape getBounds() {
		return this.bounds;
	}

	protected void setBounds(SimpleShape bounds) {
		this.bounds = bounds;
	}
}
