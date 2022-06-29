package subdivisions;

import net.devtech.jerraria.gui.api.ImGuiRenderer;
import net.devtech.jerraria.gui.api.SubdivisionState;
import net.devtech.jerraria.gui.impl.ImGuiRendererImpl;
import net.devtech.jerraria.util.math.Mat2x3f;
import net.devtech.jerraria.util.math.MatView;

public class SubdivisionTest {

	public static void main(String[] args) {
		for(int i = 0; i < 100; i++) {
			System.out.println((i/(8388608f)) - 1);
		}

		ImGuiRenderer renderer = new ImGuiRendererImpl(new Mat2x3f());

		renderer.drawSpace(10, 10);
		printStart(renderer);

		SubdivisionState state;
		try(renderer.horizontal().pop) {
			renderer.drawSpace(10, 10);
			printStart(renderer);
			renderer.drawSpace(10, 10);
			printStart(renderer);
			state = renderer.createReference();
		}

		renderer.drawSpace(10, 10);
		printStart(renderer);

		try(renderer.gotoReference(state).pop) {
			renderer.drawSpace(10, 10);
			printStart(renderer);
		}

		renderer.drawSpace(10, 10);
		printStart(renderer);
	}

	static void printStart(ImGuiRenderer renderer) {
		MatView mat = renderer.mat();
		System.out.printf("[%03.3f, %03.3f, %f]\n", mat.mulX(0, 0), mat.mulY(0, 0), mat.mulZ(0, 0, 1));
	}
}
