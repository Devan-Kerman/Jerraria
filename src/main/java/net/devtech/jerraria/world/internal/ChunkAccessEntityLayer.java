package net.devtech.jerraria.world.internal;

import java.util.Collections;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.world.EntityLayer;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class ChunkAccessEntityLayer implements EntityLayer {
	private static final Spliterator<Entity> EMPTY = Collections.<Entity>emptyList().spliterator();
	final ChunkAccessTileLayer.ChunkGetter getter;

	public ChunkAccessEntityLayer(ChunkAccessTileLayer.ChunkGetter getter) {
		this.getter = getter;
	}

	@Override
	public Stream<Entity> getEntitiesEnclosed(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
		Spliterator<Entity> spliterator = this.getEntitySpliterator(
			JMath.ifloor(fromX) << World.LOG2_CHUNK_SIZE,
			JMath.ifloor(fromY) << World.LOG2_CHUNK_SIZE,
			JMath.div(JMath.iceil(toX), World.CHUNK_SIZE),
			JMath.div(JMath.iceil(toY), World.CHUNK_SIZE));
		return StreamSupport.stream(spliterator, false).filter(entity -> entity.isEnclosed(type, fromX, fromY, toX, toY));
	}

	public Spliterator<Entity> getEntitySpliterator(int fromX, int fromY, int toX, int toY) {
		return new EntitySpliterator(fromX, fromY, toX, toY);
	}

	public class EntitySpliterator implements Spliterator<Entity> {
		int fromX, fromY, toX, toY;
		int cx, cy;
		/**
		 * max x on last y
		 */
		int maxX;
		Spliterator<Entity> current;

		public EntitySpliterator(int fromX, int fromY, int toX, int toY) {
			this.fromX = fromX;
			this.fromY = fromY;
			this.toX = toX;
			this.toY = toY;
			this.maxX = this.toX;
		}

		@Override
		public boolean tryAdvance(Consumer<? super Entity> action) {
			if(current != null && current.tryAdvance(action)) {
				return true;
			}

			if(current != null) {
				if((this.cx < this.maxX - 1) || (this.cx < this.toX && this.cy < this.toY - 1)) {
					this.cx++;
				} else if(this.cy < this.toY - 1) {
					this.cy++;
					this.cx = 0;
				} else {
					return false;
				}
			}

			Chunk chunk = getter.getChunk(cx, cy);
			current = chunk.getRawEntities().spliterator();

			return tryAdvance(action);
		}

		@Override
		public Spliterator<Entity> trySplit() {
			int xSize = this.toX - this.fromX;
			int currentIndex = (this.cy * xSize) + this.cx;
			int maxIndex = xSize * (this.toY - this.fromY) - (this.toX - this.maxX);
			int middleIndex = ((maxIndex - currentIndex) / 2) + currentIndex;
			if(middleIndex == currentIndex || xSize == 0) {
				return EMPTY;
			}
			int middleY = middleIndex / xSize, middleX = middleIndex % xSize;
			EntitySpliterator spliterator = new EntitySpliterator(middleX, middleY, this.toX, this.toY);
			spliterator.cx = middleX;
			spliterator.cy = middleY;
			this.toY = middleY+1;
			this.maxX = middleX;
			return spliterator;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return ORDERED | DISTINCT | NONNULL | IMMUTABLE;
		}
	}

}
