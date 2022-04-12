package net.devtech.jerraria.world.entity;

import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.math.SimpleShape;
import net.devtech.jerraria.world.ChunkLinkingAccess;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;

public abstract class BaseEntity extends Entity {
	SimpleShape bounds;
	int fx, fy, tx = -1, ty;
	boolean linkedLocal;

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
	protected void remove() {
		int oldX = this.oldChunkX, oldY = this.oldChunkY;
		World oldWorld = this.world;
		super.remove();
		if(oldWorld != null && this.linkedLocal) {
			ChunkLinkingAccess unlink = oldWorld.getUnsafeUnlinkingAccess(oldX, oldY);
			unlink.range(this.fx, this.fy, this.tx, this.ty);
			this.linkedLocal = false;
		}
	}

	@Override
	boolean tickPosition() {
		int oldX = this.oldChunkX, oldY = this.oldChunkY;
		World oldWorld = this.world;
		boolean moved = super.tickPosition();
		if(moved) {
			if(this.linkedLocal) {
				ChunkLinkingAccess unlink = oldWorld.getUnsafeUnlinkingAccess(oldX, oldY);
				unlink.range(this.fx, this.fy, this.tx, this.ty);
			}

			this.fx = this.getBlockX();
			this.fy = this.getBlockY();
			SimpleShape.Rectangle tx = this.bounds.maxBounds();
			this.tx = (int) (tx.width() + fx);
			this.ty = (int) (tx.height() + fy);

			ChunkLinkingAccess link = this.world.getUnsafeLinkingAccess(oldX, oldY);
			link.range(this.fx, this.fy, this.tx, this.ty);
			this.linkedLocal = true;
		}
		return moved;
	}

	protected SimpleShape getBounds() {
		return this.bounds;
	}

	protected void setBounds(SimpleShape bounds) {
		this.bounds = bounds;
	}
}
