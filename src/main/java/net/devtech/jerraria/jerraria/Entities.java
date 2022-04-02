package net.devtech.jerraria.jerraria;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.world.entity.BaseEntity;
public interface Entities {

	Registry<BaseEntity.Type<?>> REGISTRY = new Registry.Fast<>(null);

	Void __CLINIT__ = Validate.create(() -> {
		return null;
	});

	private static <T extends BaseEntity.Type<?>> T register(T tile, String name) {
		return REGISTRY.register(Id.createFull("jerraria", name), tile);
	}
}
