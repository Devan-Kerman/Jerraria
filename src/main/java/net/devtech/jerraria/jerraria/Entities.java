package net.devtech.jerraria.jerraria;

import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.world.entity.Entity;
public interface Entities {

	Registry<Entity.Type<?>> REGISTRY = new Registry.Fast<>(null);

	Void __CLINIT__ = Validate.create(() -> {
		return null;
	});

	private static <T extends Entity.Type<?>> T register(T tile, String name) {
		return REGISTRY.register(Id.createFull("jerraria", name), tile);
	}
}
