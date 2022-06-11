package net.devtech.jerraria.world.entity.render;

import net.devtech.jerraria.world.EntitySearchType;
import net.devtech.jerraria.world.entity.Entity;

public abstract class AbstractEntityRenderer<T extends Entity> implements EntityRenderer {
	protected final T entity;

	public AbstractEntityRenderer(T entity) {
		this.entity = entity;
	}
}
