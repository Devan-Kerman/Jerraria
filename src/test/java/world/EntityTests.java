package world;

import net.devtech.jerraria.entity.Entity;
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
	}
}
