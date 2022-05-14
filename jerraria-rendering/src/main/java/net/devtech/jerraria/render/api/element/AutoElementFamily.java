package net.devtech.jerraria.render.api.element;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.element.ShapeStrat;

public final class AutoElementFamily implements AutoStrat {
	public final ShapeStrat byte_, short_, int_;
	public final DrawMethod method;

	public AutoElementFamily(
		ShapeStrat byte_, ShapeStrat short_, ShapeStrat int_, DrawMethod method) {
		this.byte_ = byte_;
		this.short_ = short_;
		this.int_ = int_;
		this.method = method;
	}

	@Override
	public DrawMethod getDrawMethod() {
		return this.method;
	}

	public ShapeStrat forCount(int count) {
		ShapeStrat strat;
		if(count > 65536) {
			strat = this.int_;
		} else if(count > 256) {
			strat = this.short_;
		} else {
			strat = this.byte_;
		}
		strat.ensureCapacity(count);
		return strat;
	}
}
