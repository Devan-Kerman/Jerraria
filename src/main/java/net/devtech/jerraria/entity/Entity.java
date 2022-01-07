package net.devtech.jerraria.entity;

import java.util.function.Function;

import net.devtech.jerraria.content.Items;
import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Pos;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;

public class Entity {
	/**
	 * value of {@link #oldChunkX} if the entity has not moved chunks yet
	 */
	private static final int HAS_NOT_MOVED_CHUNK_COORD = Integer.MIN_VALUE;
	/**
	 * Entity that has no position in the world
	 */
	private static final int HOMELESS_CHUNK_COORD = Integer.MIN_VALUE + 1;

	// it's actually impossible for -int_max to be a valid chunk coordinate
	int oldChunkX = HOMELESS_CHUNK_COORD, oldChunkY;
	Type<?> type;
	double x, y, dx, dy;
	World world;

	public Entity(Type<?> type) {
		this.type = type;
	}

	protected Entity(Type<?> type, JCElement<?> element, World world, double x, double y) {
		this.type = type;
		this.world = world;
		this.x = x;
		this.y = y;
	}

	protected void setType(Type<?> type) {
		this.type = type;
	}

	public Type<?> getType() {
		return this.type;
	}

	public final void updatePosition(World world, double x, double y) {
		if(this.oldChunkX == HAS_NOT_MOVED_CHUNK_COORD) {
			// record which chunk the entity is currently stored in
			this.oldChunkX = this.getChunkX();
			this.oldChunkX = this.getChunkY();
		}

		this.world = world; // todo check if world changed,
		this.x = x;
		this.y = y;

		if(this.oldChunkX == HOMELESS_CHUNK_COORD) {
			this.tickPosition(false);
		}
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
	 * used only when a chunk moves groups, and nothing else
	 */
	final void setWorld(World world) {
		this.world = world;
	}

	void tickPosition(boolean exitOldChunk) {
		int cx = this.getChunkX(), cy = this.getChunkY();
		if(cx == this.oldChunkX && cy == this.oldChunkY) {
			return; // entity has not actually moved
		}

		if(exitOldChunk && this.oldChunkX > HOMELESS_CHUNK_COORD) {
			this.world.executeAt(this.oldChunkX, this.oldChunkY).thenAccept(w -> {
				Chunk chunk = ((AbstractWorld) w).getChunk(this.oldChunkX, this.oldChunkY);
				chunk.removeEntity(this);
			});
			this.oldChunkX = HAS_NOT_MOVED_CHUNK_COORD;
		}
		this.world.executeAt(cx, cy).thenAccept(w -> {
			Chunk chunk = ((AbstractWorld) w).getChunk(cx, cy);
			chunk.addEntity(this);
		});
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

	private int getChunkX() {
		return this.getBlockX() >> World.LOG2_CHUNK_SIZE;
	}

	private int getChunkY() {
		return this.getBlockY() >> World.LOG2_CHUNK_SIZE;
	}

	public static <T> Type<T> createType(Function<Entity, JCElement<T>> serializer, EntityDeserializer<T> deserializer) {
		return new Type<>(serializer, deserializer);
	}

	public interface EntityDeserializer<T> {
		Entity deserialize(Type<?> type, JCElement<T> element, World world, double x, double y);
	}

	public static final class Type<T> extends DefaultIdentifiedObject {
		private final Function<Entity, JCElement<T>> serializer;
		private final EntityDeserializer<T> deserializer;

		public Type(Function<Entity, JCElement<T>> serializer, EntityDeserializer<T> deserializer) {
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
