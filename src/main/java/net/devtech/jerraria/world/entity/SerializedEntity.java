package net.devtech.jerraria.world.entity;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.util.math.Vec2d;
import net.devtech.jerraria.jerracode.element.JCElement;

public class SerializedEntity implements Pair<Vec2d, JCElement> {
	public final double x;
	public final double y;
	public final JCElement<?> data;

	public SerializedEntity(double x, double y, JCElement data) {
		this.x = x;
		this.y = y;
		this.data = data;
	}

	public SerializedEntity(Vec2d pos, JCElement data) {
		this.x = pos.x();
		this.y = pos.y();
		this.data = data;
	}

	@Override
	public Vec2d left() {
		return new Vec2d(x, y);
	}

	@Override
	public JCElement right() {
		return data;
	}
}
