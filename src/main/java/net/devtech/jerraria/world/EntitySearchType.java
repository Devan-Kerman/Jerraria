package net.devtech.jerraria.world;

public interface EntitySearchType {
	enum Standard {
		ENTITY_COLLISION,
		API_ACCESS,
		PROJECTILE_ATTACK,
		MELEE_ATTACK,
		UNKNOWN
	}
}
