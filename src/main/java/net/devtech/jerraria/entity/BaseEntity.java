package net.devtech.jerraria.entity;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.devtech.jerraria.content.Items;
import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Pos;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.Server;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class BaseEntity {
	/**
	 * Entity that has no position in the world
	 */
	private static final int HOMELESS_CHUNK_COORD = Integer.MIN_VALUE;

	// it's actually impossible for -int_max to be a valid chunk coordinate
	/**
	 * the chunk the entity is currently stored in
	 */
	int oldChunkX = HOMELESS_CHUNK_COORD, oldChunkY, oldWorldId;

	Type<?> type;
	double x, y;
	World world;

	public BaseEntity(Type<?> type) {
		this.type = type;
	}

	protected BaseEntity(Type<?> type, JCElement<?> element, World world, double x, double y) {
		this.type = type;
		this.world = world;
		this.x = x;
		this.y = y;
	}

	public Type<?> getType() {
		return this.type;
	}

	protected final void updatePosition(World world, double x, double y) {
		this.world = world;
		this.x = x;
		this.y = y;

		if(this.oldChunkX == HOMELESS_CHUNK_COORD) {
			this.tickPosition(false, false);
		}
	}

	public Pos getPos() {
		return new Pos(x, y);
	}

	public int getBlockX() {
		return (int) Math.floor(this.x);
	}

	public int getBlockY() {
		return (int) Math.floor(this.y);
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public World getWorld() {
		return this.world;
	}

	public static <T> Type<T> createType(Function<BaseEntity, JCElement<T>> serializer, Deserializer<T> deserializer) {
		return new Type<>(serializer, deserializer);
	}

	public interface Deserializer<T> {
		BaseEntity deserialize(Type<?> type, JCElement<T> element, World world, double x, double y);
	}

	protected void setType(Type<?> type) {
		this.type = type;
	}

	/**
	 * @return true if the entity is currently in a world
	 */
	public boolean inWorld() {
		return this.world != null;
	}

	/**
	 * used only when a chunk moves groups, and nothing else
	 */
	final void moveGroup(World world) {
		this.world = world;
	}

	protected final void remove() {
		this.tickPosition(true, true);
		this.world = null;
	}

	// todo redo tick position
	// instead, chunks will be responsible for removing entities that they don't contain themselves
	// so after a chunk ticks, it checks to ensure all the entities in it's list are still within the chunk
	// for all entities that have moved, they then executeAt & move chunks

	// chunks will also have to automatically filter out removed entities from their getEntities method

	void tickPosition(boolean exitOldChunk, boolean isRemove) {
		int cx = this.getChunkX(), cy = this.getChunkY();
		if(cx == this.oldChunkX && cy == this.oldChunkY && this.world.sessionId() == this.oldWorldId) {
			return; // entity has not actually moved
		}

		// remove entity from the chunk's entity list
		if(exitOldChunk && this.oldChunkX != HOMELESS_CHUNK_COORD) {
			int oldX = this.oldChunkX, oldY = this.oldChunkY, oldWorld = this.oldWorldId;
			World world;
			if(this.world.sessionId() == oldWorld && this.world.canAccessImmediately(oldX, oldY)) {
				world = this.world;
			} else {
				Server server = this.world.getServer();
				world = server.getById(this.oldWorldId);
			}

			world.executeAt(oldX * World.CHUNK_SIZE, oldY * World.CHUNK_SIZE).thenAccept(w -> {
				Chunk chunk = ((AbstractWorld) w).getChunk(oldX, oldY);
				chunk.removeEntity(this);
			});
		}

		if(isRemove) {
			this.world = null;
			this.oldChunkX = HOMELESS_CHUNK_COORD;
		} else {
			this.world.executeAt(cx * World.CHUNK_SIZE, cy * World.CHUNK_SIZE).thenAccept(w -> {
				Chunk chunk = ((AbstractWorld) w).getChunk(cx, cy);
				chunk.addEntity(this);
				this.oldChunkX = cx;
				this.oldChunkY = cy;
				this.world = w;
			});
		}
	}

	private int getChunkX() {
		return this.getBlockX() >> World.LOG2_CHUNK_SIZE;
	}

	private int getChunkY() {
		return this.getBlockY() >> World.LOG2_CHUNK_SIZE;
	}

	public static final class Type<T> extends DefaultIdentifiedObject {
		private final Function<BaseEntity, JCElement<T>> serializer;
		private final Deserializer<T> deserializer;

		public Type(Function<BaseEntity, JCElement<T>> serializer, Deserializer<T> deserializer) {
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		public JCElement<T> serialize(BaseEntity instance) {
			return this.serializer.apply(instance);
		}

		public BaseEntity deserialize(JCElement<T> element, World world, double x, double y) {
			return this.deserializer.deserialize(this, element, world, x, y);
		}

		@Override
		protected Registry<?> getValidRegistry() {
			return Items.REGISTRY;
		}
	}
}
