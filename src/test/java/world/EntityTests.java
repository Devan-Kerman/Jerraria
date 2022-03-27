package world;

import net.devtech.jerraria.entity.BaseEntity;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class EntityTests {

	static class ChunkEntity extends BaseEntity {
		final Chunk chunk;
		public ChunkEntity(Chunk chunk) {
			super(null);
			this.chunk = chunk;
		}

		@Override
		public String toString() {
			return "[" + chunk.getChunkX() + ',' + chunk.getChunkY() + ']';
		}
	}
}
