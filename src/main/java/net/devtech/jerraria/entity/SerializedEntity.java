package net.devtech.jerraria.entity;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.util.Pos;
import net.devtech.jerraria.util.data.element.JCElement;

public class SerializedEntity implements Pair<Pos, JCElement> {
	public final double x;
	public final double y;
	public final JCElement data;

	public SerializedEntity(double x, double y, JCElement data) {
		this.x = x;
		this.y = y;
		this.data = data;
	}

	public SerializedEntity(Pos pos, JCElement data) {
		this.x = pos.x();
		this.y = pos.y();
		this.data = data;
	}

	@Override
	public Pos left() {
		return new Pos(x, y);
	}

	@Override
	public JCElement right() {
		return data;
	}
}
