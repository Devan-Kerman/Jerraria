import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import javax.swing.JFrame;

public class Test {
	public static void main(String[] args) {
		List<Double> random = DoubleStream.generate(Math::random).limit(1048576).boxed().toList();
		conductTest(random);
		conductTest(random);
	}

	private static void conductTest(List<Double> random) {
		double output = 0;
		long start = System.currentTimeMillis();
		for(int i = 0; i < 50; i++) {
			for(Double d : random) {
				//output += (Math.signum(d) + 1) / 2;
				output += d > 0 ? 1 : 0;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println(output);
		System.out.println(end - start);
	}
}
