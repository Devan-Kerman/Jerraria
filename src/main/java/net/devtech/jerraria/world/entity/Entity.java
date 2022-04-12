package net.devtech.jerraria.world.entity;

import java.util.Objects;
import java.util.function.Function;

import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.jerraria.Items;
import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.Positioned;
import net.devtech.jerraria.util.math.Vec2d;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.render.EntityRenderer;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public abstract class Entity implements Positioned {
	/**
	 * this states the entity does not belong to a chunk
	 */
	public static final int HOBO_CHUNK_POS = Integer.MIN_VALUE;
	int oldChunkX = HOBO_CHUNK_POS, oldChunkY, oldWorldId;

	Type<?> type;
	double x, y;
	World world;
	double dx;
	double dy;
	EntityRenderer renderer;


	public Entity(Type<?> type) {
		this.type = type;
	}

	protected Entity(Type<?> type, JCElement<?> element, World world, double x, double y) {
		this.type = type;
		this.world = world;
		this.x = x;
		this.y = y;
	}

	public Type<?> getType() {
		return this.type;
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

	public EntityRenderer getRenderer() {
		EntityRenderer renderer = this.renderer;
		if(renderer == null) {
			this.renderer = renderer = Objects.requireNonNull(
				this.createRenderer(),
				"Cannot have null entity renderer!"
			);
		}
		return renderer;
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

	/**
	 * @return true if the entity is entirely enclosed by the given bounding box
	 */
	public abstract boolean isEnclosed(EntitySearchType type, double fromX, double fromY, double toX, double toY);

	/**
	 * @return true if the entity contacts the given bounding box
	 */
	public abstract boolean doesIntersect(EntitySearchType type, double fromX, double fromY, double toX, double toY);

	public static <T> Type<T> createType(Function<Entity, JCElement<T>> serializer, Deserializer<T> deserializer) {
		return new Type<>(serializer, deserializer);
	}

	public Vec2d getPos() {
		return new Vec2d(this.x, this.y);
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

	public boolean doesSaveInChunk() {
		return true;
	}

	protected final void updatePosition(World world, double x, double y) {
		Validate.notNull(world, "World cannot be null!");
		boolean inWorld = this.inWorld();
		this.world = world;
		this.x = x;
		this.y = y;

		if(!inWorld) {
			this.tickPosition();
		}
	}

	protected void remove() {
		this.world = null;
		this.oldChunkX = HOBO_CHUNK_POS;
	}

	protected abstract EntityRenderer createRenderer();

	protected void tick() {
		// tick stuff
	}

	/**
	 * used only when a chunk moves groups, and nothing else
	 */
	final void moveGroup(World world) {
		this.world = world;
	}

	boolean isHomeChunk(Chunk chunk) {
		return this.oldChunkX == chunk.getChunkX() && this.oldChunkY == chunk.getChunkY() && this.oldWorldId == chunk
			.getWorld()
			.sessionId();
	}

	boolean tickPosition() {
		int cx = this.getChunkX(), cy = this.getChunkY();
		if(cx == this.oldChunkX && cy == this.oldChunkY && this.world.sessionId() == this.oldWorldId) {
			return false; // entity has not actually moved
		}

		this.world.executeAt(cx * World.CHUNK_SIZE, cy * World.CHUNK_SIZE).thenAccept(w -> {
			Chunk chunk = ((AbstractWorld) w).getChunk(cx, cy);
			chunk.addEntity(this);
			this.oldChunkX = cx;
			this.oldChunkY = cy;
			this.world = w;
		});
		return true;
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

	public interface Deserializer<T> {
		Entity deserialize(Type<?> type, JCElement<T> element, World world, double x, double y);
	}

	public static final class Type<T> extends DefaultIdentifiedObject {
		private final Function<Entity, JCElement<T>> serializer;
		private final Deserializer<T> deserializer;

		public Type(Function<Entity, JCElement<T>> serializer, Deserializer<T> deserializer) {
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		public JCElement<T> serialize(Entity instance) {
			return this.serializer.apply(instance);
		}

		public Entity deserialize(JCElement<T> element, World world, double x, double y) {
			return this.deserializer.deserialize(this, element, world, x, y);
		}

		@Override
		protected Registry<?> getValidRegistry() {
			return Items.REGISTRY;
		}
	}
}
