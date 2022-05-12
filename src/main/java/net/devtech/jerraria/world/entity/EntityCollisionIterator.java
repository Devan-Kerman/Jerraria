package net.devtech.jerraria.world.entity;

import java.util.Iterator;
import java.util.List;

import net.devtech.jerraria.util.math.Pos2d;
import net.devtech.jerraria.util.math.Rectangle;
import net.devtech.jerraria.util.math.Vec2d;
import net.devtech.jerraria.util.math.Vec2i;

public class EntityCollisionIterator implements Iterator<EntityCollisionIterator.Intersection> {
	final Pos2d entity;
	final List<Rectangle> hitboxes;
	final Iterator<Rectangle> boxes;
	final Intersection mut;
	final float startTimeFraction;
	float rectangleTimeFraction;

	Rectangle current;
	int index, finalIndex;

	public EntityCollisionIterator(Pos2d entity, List<Rectangle> boxes, Intersection mut, float fraction) {
		this.entity = entity;
		this.boxes = boxes.listIterator();
		this.hitboxes = boxes;
		this.mut = mut;
		this.startTimeFraction = fraction;
	}

	public static void main(String[] args) {
		Vec2d.Mut entity = new Vec2d.Mut(.33f, .33f);
		Vec2d.Mut delta = new Vec2d.Mut(.33f, .33f);
		List<Rectangle> rectangles = List.of(new Rectangle(0, 0, 1, 1));

		double etaXIntersect = Float.POSITIVE_INFINITY, etaYIntersect = Float.POSITIVE_INFINITY;
		for(Rectangle rectangle : rectangles) {
			double yDirection = delta.y() > 0 ? 1 : 0;
			double cornerY = rectangle.offY() + rectangle.height() * yDirection;
			double positionY = cornerY + entity.y() + delta.y() * etaYIntersect;
			int blockY = (int) (Math.floor(positionY) + yDirection);
			double yDistanceToNextBlock = blockY - positionY;
			double yTimeToNextBlock = yDistanceToNextBlock / delta.y();

			double xDirection = delta.x() > 0 ? 1 : 0;
			double cornerX = rectangle.offX() + rectangle.width() * xDirection;
			double positionX = cornerX + entity.x() + delta.x() * etaXIntersect;
			int blockX = (int) (Math.floor(positionX) + xDirection);
			double xDistanceToNextBlock = blockX - positionX;
			double xTimeToNextBlock = xDistanceToNextBlock / delta.x();

			if(etaXIntersect > xTimeToNextBlock) {
				etaXIntersect = xTimeToNextBlock;
			}

			if(etaYIntersect > yTimeToNextBlock) {
				etaYIntersect = yTimeToNextBlock;
			}

			// basically, for velocity changes over time we should make each block return how much it would change
			//  the entity's velocity given a time, and then add them together
			System.out.println(xTimeToNextBlock + " " + yTimeToNextBlock);
		}

		while(etaXIntersect < 1 && etaYIntersect < 1) {
			if(etaYIntersect < etaXIntersect) { // travel along y direction first
				for(Rectangle rectangle : rectangles) {



				}
			} else { // travel along x direction first
				for(Rectangle rectangle : rectangles) {


				}
			}
		}
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Intersection next() {
		return this.mut;
	}

	Rectangle currentBox() {
		if(this.index == this.finalIndex) { // next
			if(this.boxes.hasNext()) {
				this.current = this.boxes.next();

			}
		}
		return null;
	}

	protected boolean first() {
		return false;
	}

	public static final class Intersection extends Vec2i.Mut {
		public Rectangle collisionRectangle;
		public float contactTimeFraction;

		public Intersection() {
		}

		public Intersection(int x, int y) {
			super(x, y);
		}
	}
}
