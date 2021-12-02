package net.devtech.jerraria.world.entity;

import net.devtech.jerraria.content.Entities;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.world.World;

public class EntityInternal {
	public static Entity deserialize(World world, Id.Full id, SerializedEntity element) {
		Entity.Type<?> type = Entities.REGISTRY.getForId(id);
		return type.deserialize(element.data, world, element.x, element.y);
	}

	public static SerializedEntity serialize(Entity entity) {
		return new SerializedEntity(entity.getX(), entity.getY(), entity.type.serialize(entity));
	}

	public static void setWorld(Entity entity, World world) {
		entity.setWorld(world);
	}

	public static void tickPos(Entity entity) {
		entity.tickPosition(true);
	}
}
