package net.devtech.jerraria.render.api.element;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.element.Quad;
import net.devtech.jerraria.render.internal.element.Seq;

public sealed interface AutoStrat permits AutoElementFamily {
	AutoStrat DEFAULT = sequence(DrawMethod.TRIANGLE);
	AutoStrat QUADS = new AutoElementFamily(Quad.BYTE_, Quad.SHORT_, Quad.INT_, DrawMethod.TRIANGLE, "QUADS");

	static AutoStrat sequence(DrawMethod method) {
		return Seq.SEQUENCES[method.ordinal()];
	}

	/**
	 * @return The gl drawing method to draw this strategy
	 */
	DrawMethod getDrawMethod();

	int vertexCount();

	int minimumVertices();

	int elementsForVertexData(int count);
}
