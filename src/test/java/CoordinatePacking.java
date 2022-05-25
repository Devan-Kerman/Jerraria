import net.devtech.jerraria.util.math.JMath;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoordinatePacking {
	@Test
	public void test() {
		this.test(0, 0);
		this.test(-1, 0);
		this.test(0, -1);
		this.test(-1, -1);

		this.test(-1024, 0);
		this.test(0, -1024);
		this.test(-1024, -1024);

		this.test(-1024, 0);
		this.test(0, -1024);
		this.test(-1, -1024);

		this.test(1, 0);
		this.test(0, 1);
		this.test(1, 1);

		this.test(1024, 0);
		this.test(0, 1024);
		this.test(1024, 1024);
	}

	protected void test(int a, int b) {
		long id = JMath.combineInts(a, b);
		Assertions.assertEquals(a, Chunk.getA(id));
		Assertions.assertEquals(b, Chunk.getB(id));
	}
}
