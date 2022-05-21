package net.devtech.jerraria.client;

import static java.util.Objects.requireNonNull;

import net.devtech.jerraria.util.math.Matrix3f;

public class ClientMain {

	public static void main(String[] argv) {
		Bootstrap.startClient(argv, () -> {




			return null;
		});
	}

	public static Matrix3f cartesianToAWTIndexGrid(float scale) {
		int[] dims = ClientInit.dims;
		Matrix3f cartToIndexMat = new Matrix3f();
		cartToIndexMat.offset(-1, 1);
		cartToIndexMat.scale(2, -2);
		cartToIndexMat.scale(dims[1] / (dims[0] * scale), 1 / scale);
		return cartToIndexMat;
	}
}
