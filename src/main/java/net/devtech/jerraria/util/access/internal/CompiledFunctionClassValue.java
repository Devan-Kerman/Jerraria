package net.devtech.jerraria.util.access.internal;

import net.devtech.jerraria.util.func.ArrayFunc;

public class CompiledFunctionClassValue<F> extends ClassValue<FunctionCompiler<F>> {
	private final ArrayFunc<F> compiler;
	private final F empty;

	public CompiledFunctionClassValue(ArrayFunc<F> compiler, F empty) {
		this.compiler = compiler;
		this.empty = empty;
	}

	@Override
	protected FunctionCompiler<F> computeValue(Class<?> type) {
		return new FunctionCompiler<>(this.compiler, this.empty);
	}
}
