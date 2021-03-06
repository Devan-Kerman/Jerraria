package net.devtech.jerraria.jerraria;

import net.devtech.jerraria.jerraria.item.AirItem;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.world.item.IdentityItem;
import net.devtech.jerraria.world.item.Item;

public interface Items {
	IdentityItem AIR = AirItem.INSTANCE;

	Registry<Item.Type<?>> REGISTRY = new Registry.Fast<>(AIR);

	Void __CLINIT__ = Validate.create(() -> {
		register(AIR, "air");
		return null;
	});

	private static <T extends Item.Type<?>> T register(T tile, String name) {
		return REGISTRY.register(Id.createFull("jerraria", name), tile);
	}
}
