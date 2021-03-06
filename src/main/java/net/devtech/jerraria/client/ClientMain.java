package net.devtech.jerraria.client;

import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;

public class ClientMain {
	public static void main(String[] argv) {
		Bootstrap.startClient(argv, () -> {

			return null;
		});
	}

	public static Mat cartesianToAWTIndexGrid(float scale) {
		int[] dims = ClientInit.dims;
		Mat cartToIndexMat = Mat.create();
		cartToIndexMat.offset(-1, 1);
		cartToIndexMat.scale(2, -2);
		cartToIndexMat.scale(dims[1] / (dims[0] * scale), 1 / scale);
		return cartToIndexMat;
	}
}
