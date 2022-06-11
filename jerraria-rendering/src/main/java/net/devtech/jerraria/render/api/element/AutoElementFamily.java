package net.devtech.jerraria.render.api.element;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.element.ShapeStrat;

public final class AutoElementFamily implements AutoStrat {
	public final ShapeStrat byte_, short_, int_;
	public final DrawMethod method;
	public final String name;
	public final boolean forceRestart;

	public AutoElementFamily(
		ShapeStrat byte_, ShapeStrat short_, ShapeStrat int_, DrawMethod method, String name, boolean forceRestart) {
		this.byte_ = byte_;
		this.short_ = short_;
		this.int_ = int_;
		this.method = method;
		this.name = name;
		this.forceRestart = forceRestart;
	}

	public AutoElementFamily(
		ShapeStrat byte_, ShapeStrat short_, ShapeStrat int_, DrawMethod method, String name) {
		this(byte_, short_, int_, method, name, false);
	}

	@Override
	public DrawMethod getDrawMethod() {
		return this.method;
	}

	@Override
	public int vertexCount() {
		return this.byte_.vertexCount(this.method);
	}

	@Override
	public int minimumVertices() {
		return this.byte_.minumumVertices(this.method);
	}

	@Override
	public int elementsForVertexData(int count) {
		return this.byte_.elementsForVertexData(count);
	}

	@Override
	public boolean forceRestart() {
		return this.forceRestart;
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
		return this.name + " via " + this.method;
	}
}
