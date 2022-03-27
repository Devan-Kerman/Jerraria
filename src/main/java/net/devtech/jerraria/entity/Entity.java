package net.devtech.jerraria.entity;

import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.World;

public class Entity extends BaseEntity {
	double dx, dy;

	public Entity(Type<?> type) {
		super(type);
	}

	protected Entity(Type<?> type, JCElement<?> element, World world, double x, double y) {
		super(type, element, world, x, y);
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
}
