package world;

import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class EntityTests {

	static class ChunkEntity extends Entity {
		final Chunk chunk;
		public ChunkEntity(Chunk chunk) {
			super(null);
			this.chunk = chunk;
		}

		@Override
		public String toString() {
			return "[" + chunk.getChunkX() + ',' + chunk.getChunkY() + ']';
		}

		@Override
		public boolean isEnclosed(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
			return false;
		}

		@Override
		public boolean doesIntersect(EntitySearchType type, double fromX, double fromY, double toX, double toY) {
			return false;
		}
	}
}
