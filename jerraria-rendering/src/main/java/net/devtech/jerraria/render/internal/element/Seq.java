package net.devtech.jerraria.render.internal.element;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static net.devtech.jerraria.util.math.JMath.ifloor;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.element.AutoElementFamily;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.internal.BufferBuilder;

public final class Seq extends ShapeStrat {
	public static final Seq BYTE_ = new Seq(1, GL_UNSIGNED_BYTE, ShapeStrat.BYTE);
	public static final Seq SHORT_ = new Seq(2, GL_UNSIGNED_SHORT, ShapeStrat.SHORT);
	public static final Seq INT_ = new Seq(4, GL_UNSIGNED_INT, ShapeStrat.INT);
	public static final AutoStrat[] SEQUENCES = new AutoStrat[DrawMethod.VALUES.size()];
	static {
		for(DrawMethod value : DrawMethod.VALUES) {
			SEQUENCES[value.ordinal()] = new AutoElementFamily(Seq.BYTE_, Seq.SHORT_, Seq.INT_, value);
		}
	}

	Seq(int unitLen, int glType, BufferInserter inserter) {
		super(new BufferBuilder(unitLen, ifloor(min(pow(256, unitLen), 16384))*unitLen), inserter, glType);
		this.ensureCapacity(ifloor(min(pow(256, unitLen), 16384)));
	}

	@Override
	public int elementsForVertexData(int count) {
		return count;
	}

	@Override
	void ensureCapacity0(int vertices) {
		ByteBuffer buffer = this.builder.getBuffer();
		for(int i = this.builder.getVertexCount(); i < vertices; i++) {
			this.inserter.insert(buffer, i);
			this.builder.next();
		}
	}
}
