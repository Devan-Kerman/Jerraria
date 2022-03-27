package net.devtech.jerraria.entity;

import net.devtech.jerraria.content.Entities;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EntityInternal {
	public static BaseEntity deserialize(World world, Id.Full id, SerializedEntity element) {
		BaseEntity.Type<?> type = Entities.REGISTRY.getForId(id);
		return type.deserialize(element.data, world, element.x, element.y);
	}

	public static SerializedEntity serialize(BaseEntity entity) {
		return new SerializedEntity(entity.getX(), entity.getY(), entity.type.serialize(entity));
	}

	public static void setWorld(BaseEntity entity, World world) {
		entity.moveGroup(world);
	}

	public static void tickPos(BaseEntity entity) {
		entity.tickPosition();
	}

	public static void setHomeChunk(BaseEntity entity, Chunk chunk) {
		entity.setHomeChunk(chunk);
	}

	public static boolean isHomeChunk(BaseEntity entity, Chunk chunk) {
		return entity.isHomeChunk(chunk);
	}

	public static void tick(BaseEntity entity) {
		entity.tick();
	}
}
