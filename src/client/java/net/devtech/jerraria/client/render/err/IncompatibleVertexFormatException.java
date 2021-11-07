package net.devtech.jerraria.client.render.err;

public class IncompatibleVertexFormatException extends IllegalArgumentException {
	public IncompatibleVertexFormatException() {
	}

	public IncompatibleVertexFormatException(String s) {
		super(s);
	}

	public IncompatibleVertexFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncompatibleVertexFormatException(Throwable cause) {
		super(cause);
	}
}
