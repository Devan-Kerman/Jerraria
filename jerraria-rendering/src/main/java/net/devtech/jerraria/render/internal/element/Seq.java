package net.devtech.jerraria.render.internal.element;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static net.devtech.jerraria.util.math.JMath.ifloor;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.element.AutoElementFamily;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.internal.buffers.EBOBuilder;

public final class Seq extends ShapeStrat {
	public static final Seq BYTE_ = new Seq(1, GL_UNSIGNED_BYTE, ShapeStrat.BYTE);
	public static final Seq SHORT_ = new Seq(2, GL_UNSIGNED_SHORT, ShapeStrat.SHORT);
	public static final Seq INT_ = new Seq(4, GL_UNSIGNED_INT, ShapeStrat.INT);
	public static final AutoStrat[] SEQUENCES = new AutoStrat[DrawMethod.VALUES.size()];
	static {
		for(DrawMethod value : DrawMethod.VALUES) {
			SEQUENCES[value.ordinal()] = new AutoElementFamily(Seq.BYTE_, Seq.SHORT_, Seq.INT_, value, value.name());
		}
	}

	Seq(int unitLen, int glType, BufferInserter inserter) {
		super(new EBOBuilder(unitLen), inserter, glType);
		this.ensureCapacity(ifloor(min(pow(256, unitLen), 16384)));
	}

	@Override
	public int elementsForVertexData(int count) {
		return count;
	}

	@Override
	public int vertexCount(DrawMethod method) {
		return method.vertexCount;
	}

	@Override
	public int minumumVertices(DrawMethod method) {
		return method.minimumVertices;
	}

	@Override
	void ensureCapacity0(int elements) {
		for(int i = this.builder.getElementCount(); i < elements; i++) {
			this.inserter.insert(this.builder.vert(), i);
		}
	}
}
