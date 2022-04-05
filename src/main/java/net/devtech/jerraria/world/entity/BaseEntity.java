package net.devtech.jerraria.world.entity;

import java.util.function.Function;

import net.devtech.jerraria.jerraria.Items;
import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.math.Position;
import net.devtech.jerraria.util.math.Positioned;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public abstract class BaseEntity implements Positioned {
	/**
	 * this states the entity does not belong to a chunk
	 */
	public static final int HOBO_CHUNK_POS = Integer.MIN_VALUE;
	int oldChunkX = HOBO_CHUNK_POS, oldChunkY, oldWorldId;

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
		boolean inWorld = this.inWorld();

		this.world = world;
		this.x = x;
		this.y = y;

		if(!inWorld) {
			this.tickPosition();
		}
	}

	protected final void remove() {
		this.world = null;
		this.oldChunkX = HOBO_CHUNK_POS;
	}

	/**
	 * @return true if the entity is currently in a world
	 */
	public boolean inWorld() {
		return this.world != null;
	}

	/**
	 * @return true if the entity is entirely enclosed by the given bounding box
	 */
	public abstract boolean isEnclosed(EntitySearchType type, double fromX, double fromY, double toX, double toY);

	/**
	 * @return true if the entity contacts the given bounding box
	 */
	public abstract boolean doesIntersect(EntitySearchType type, double fromX, double fromY, double toX, double toY);

	public static <T> Type<T> createType(Function<BaseEntity, JCElement<T>> serializer, Deserializer<T> deserializer) {
		return new Type<>(serializer, deserializer);
	}

	public Position getPos() {
		return new Position(this.x, this.y);
	}

	public int getBlockX() {
		return (int) Math.floor(this.x);
	}

	public int getBlockY() {
		return (int) Math.floor(this.y);
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	public World getWorld() {
		return this.world;
	}

	protected void tick() {
		// tick stuff
	}

	public boolean doesSaveInChunk() {
		return true;
	}

	public interface Deserializer<T> {
		BaseEntity deserialize(Type<?> type, JCElement<T> element, World world, double x, double y);
	}

	protected void setType(Type<?> type) {
		this.type = type;
	}

	/**
	 * used only when a chunk moves groups, and nothing else
	 */
	final void moveGroup(World world) {
		this.world = world;
	}

	boolean isHomeChunk(Chunk chunk) {
		return this.oldChunkX == chunk.getChunkX() && this.oldChunkY == chunk.getChunkY() && this.oldWorldId == chunk.getWorld().sessionId();
	}

	void tickPosition() {
		int cx = this.getChunkX(), cy = this.getChunkY();
		if(cx == this.oldChunkX && cy == this.oldChunkY && this.world.sessionId() == this.oldWorldId) {
			return; // entity has not actually moved
		}

		this.world.executeAt(cx * World.CHUNK_SIZE, cy * World.CHUNK_SIZE).thenAccept(w -> {
			Chunk chunk = ((AbstractWorld) w).getChunk(cx, cy);
			chunk.addEntity(this);
			this.oldChunkX = cx;
			this.oldChunkY = cy;
			this.world = w;
		});
	}

	void setHomeChunk(Chunk chunk) {
		this.oldChunkX = chunk.getChunkX();
		this.oldChunkY = chunk.getChunkY();
		this.oldWorldId = chunk.getWorld().sessionId();
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
