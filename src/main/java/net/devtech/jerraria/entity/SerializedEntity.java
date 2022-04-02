package net.devtech.jerraria.entity;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.util.math.Position;
import net.devtech.jerraria.jerracode.element.JCElement;

public class SerializedEntity implements Pair<Position, JCElement> {
	public final double x;
	public final double y;
	public final JCElement data;

	public SerializedEntity(double x, double y, JCElement data) {
		this.x = x;
		this.y = y;
		this.data = data;
	}

	public SerializedEntity(Position pos, JCElement data) {
		this.x = pos.x();
		this.y = pos.y();
		this.data = data;
	}

	@Override
	public Position left() {
		return new Position(x, y);
	}

	@Override
	public JCElement right() {
		return data;
	}
}
