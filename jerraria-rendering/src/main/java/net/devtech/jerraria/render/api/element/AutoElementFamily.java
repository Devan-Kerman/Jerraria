package net.devtech.jerraria.render.api.element;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.element.ShapeStrat;

public final class AutoElementFamily implements AutoStrat {
	public final ShapeStrat byte_, short_, int_;
	public final DrawMethod method;
	public final String name;

	public AutoElementFamily(
		ShapeStrat byte_, ShapeStrat short_, ShapeStrat int_, DrawMethod method, String name) {
		this.byte_ = byte_;
		this.short_ = short_;
		this.int_ = int_;
		this.method = method;
		this.name = name;
	}

	@Override
	public DrawMethod getDrawMethod() {
		return this.method;
	}

	@Override
	public int vertexCount() {
		return byte_.vertexCount(this.method);
	}

	@Override
	public int minimumVertices() {
		return byte_.minumumVertices(this.method);
	}

	@Override
	public int elementsForVertexData(int count) {
		return byte_.elementsForVertexData(count);
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

	@Override
	public String toString() {
		return this.name;
	}
}