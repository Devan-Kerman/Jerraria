package net.devtech.jerraria.world.entity;

import net.devtech.jerraria.jerraria.Entities;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EntityInternal {
	public static Entity deserialize(World world, Id.Full id, SerializedEntity element) {
		Entity.Type<?> type = Entities.REGISTRY.getForId(id);
		return type.deserialize(element.data, world, element.x, element.y);
	}

	public static SerializedEntity serialize(Entity entity) {
		return new SerializedEntity(entity.x(), entity.y(), entity.type.serialize(entity));
	}

	public static void setWorld(Entity entity, World world) {
		entity.moveGroup(world);
	}

	public static void tickPos(Entity entity) {
		entity.tickPosition();
	}

	public static void setHomeChunk(Entity entity, Chunk chunk) {
		entity.setHomeChunk(chunk);
	}

	public static boolean isHomeChunk(Entity entity, Chunk chunk) {
		return entity.isHomeChunk(chunk);
	}

	public static void tick(Entity entity) {
		entity.tick();
	}
}
