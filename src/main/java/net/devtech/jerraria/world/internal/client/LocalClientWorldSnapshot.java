package net.devtech.jerraria.world.internal.client;

import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.WorldServer;
import net.devtech.jerraria.world.internal.DelegateWorldServer;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class LocalClientWorldSnapshot extends AbstractClientWorld {
	final int cacheX, cacheY;
	final ClientChunk[] cache;

	static class LocalClientServerSnapshot extends DelegateWorldServer {
		LocalClientWorldSnapshot world;

		public LocalClientServerSnapshot(WorldServer delegate) {
			super(delegate);
		}

		@Override
		public World getById(int sessionId) {
			return sessionId == this.world.sessionId ? this.world : null;
		}
	}

	public LocalClientWorldSnapshot(WorldServer server, int id, int x, int y, ClientChunk[] cache) {
		super(new LocalClientServerSnapshot(server), id);
		this.cacheX = x;
		this.cacheY = y;
		this.cache = cache;
		((LocalClientServerSnapshot)this.getServer()).world = this;
	}

	@Override
	public boolean isChunkLoaded(int cx, int cy) {
		if(this.cacheX >= cx && this.cacheY >= cy) {
			int index = (cx - this.cacheX) * 2 + (cy - this.cacheY);
			return index < this.cache.length;
		}
		return false;
	}

	@Override
	public boolean isChunkAccessible(int cx, int cy) {
		return this.isChunkLoaded(cx, cy);
	}

	@Override
	public Chunk getChunk(int x, int y) {
		if(this.cacheX >= x && this.cacheY >= y) {
			int index = (x - this.cacheX) * 2 + (y - this.cacheY);
			if(index < this.cache.length) {
				return this.cache[index];
			}
		}
		return null;
	}
}
