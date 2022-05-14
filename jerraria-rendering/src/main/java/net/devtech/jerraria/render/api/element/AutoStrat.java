package net.devtech.jerraria.render.api.element;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.element.LineStrip;
import net.devtech.jerraria.render.internal.element.Quad;
import net.devtech.jerraria.render.internal.element.Seq;
import net.devtech.jerraria.render.internal.element.TriStrip;

/**
 * A {@link AutoStrat} is a description on how to convert a custom primitive type to multiple opengl primitiives via
 * element buffers. For example, the quad primitive was removed in opengl, so we can use {@link AutoStrat#QUADS} to convert 4
 * vertex datas into 6 vertices (2 triangles).
 */
public sealed interface AutoStrat permits AutoElementFamily {
	/**
	 * The default rendering method
	 */
	AutoStrat TRIANGLE = sequence(DrawMethod.TRIANGLE);

	/**
	 * Renders Quads with {@link DrawMethod#TRIANGLE}s
	 */
	AutoStrat QUADS = new AutoElementFamily(Quad.BYTE_, Quad.SHORT_, Quad.INT_, DrawMethod.TRIANGLE, "QUADS");

	/**
	 * Renders {@link DrawMethod#TRIANGLE_STRIP}s with {@link DrawMethod#TRIANGLE}s
	 */
	AutoStrat TRIANGLE_STRIP = new AutoElementFamily(TriStrip.BYTE_, TriStrip.SHORT_, TriStrip.INT_, DrawMethod.TRIANGLE, "TRIANGLE_STRIP");

	/**
	 * Renders {@link DrawMethod#LINE_STRIP}s with {@link DrawMethod#LINES}s
	 */
	AutoStrat LINE_STRIP = new AutoElementFamily(LineStrip.BYTE_, LineStrip.SHORT_, LineStrip.INT_, DrawMethod.LINES, "LINE_STRIP");

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
