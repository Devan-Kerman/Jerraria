package net.devtech.jerraria.world.entity;

import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.math.SimpleShape;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;

public abstract class BaseEntity extends Entity {
	SimpleShape bounds;

	public BaseEntity(Type<?> type, SimpleShape bounds) {
		super(type);
		this.bounds = bounds;
	}

	protected BaseEntity(Type<?> type, JCElement<?> element, World world, double x, double y, SimpleShape bounds) {
		super(type, element, world, x, y);
		this.bounds = bounds;
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

	@Override
	protected void tick() {
		// move entity if linked properly, we do this in beginning because the entity changes
		super.tick();
		
	}

	protected SimpleShape getBounds() {
		return this.bounds;
	}

	protected void setBounds(SimpleShape bounds) {
		this.bounds = bounds;
	}
}
