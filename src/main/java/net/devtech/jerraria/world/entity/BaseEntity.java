package net.devtech.jerraria.world.entity;

import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.math.Rectangle;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;

import java.util.Iterator;

public abstract class BaseEntity extends Entity {
	Iterable<Rectangle> bounds;
	Rectangle enclosingBounds;

	double dx, dy; // todo serialize

	public BaseEntity(Type<?> type, Iterable<Rectangle> bounds) {
		super(type);
		this.bounds = bounds;
	}

	protected BaseEntity(Type<?> type, JCElement<?> element, World world, double x, double y, Iterable<Rectangle> bounds) {
		super(type, element, world, x, y);
		this.bounds = bounds;
	}

	@Override
	public boolean isEnclosed(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
		double x = this.getX(), y = this.getY();
		for (Rectangle next : getCollisionBounds()) {
			if(!next.isEnclosed(fromX - x, fromY - y, toX - x, toY - y)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean doesIntersect(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
		double x = this.getX(), y = this.getY();
		for (Rectangle next : getCollisionBounds()) {
			if(next.doesIntersect(fromX - x, fromY - y, toX - x, toY - y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void tick() {
		// move entity if linked properly, we do this in beginning because the entity changes
		super.tick();
		for (Rectangle bound : getCollisionBounds()) {

		}
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

	public final Iterable<Rectangle> getCollisionBounds() {
		return this.bounds;
	}

	public final Rectangle getEnclosingBounds() {
		return this.enclosingBounds;
	}

	protected void setCollisionBounds(Iterable<Rectangle> bounds) {
		double offX = Double.POSITIVE_INFINITY, offY = Double.POSITIVE_INFINITY, endX = Double.NEGATIVE_INFINITY, endY = Double.NEGATIVE_INFINITY;
		for (Rectangle bound : bounds) {
			if(bound.offX() < offX) {
				offX = bound.offX();
			}
			if(bound.offY() < offY) {
				offY = bound.offY();
			}

			if(bound.endX() > endX) {
				endX = bound.endX();
			}

			if(bound.endY() > endY) {
				endY = bound.endY();
			}
		}
		this.enclosingBounds = new Rectangle(offX, offY, endX - offX, endY - offY);
		this.bounds = bounds;
	}
}
