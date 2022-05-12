package net.devtech.jerraria.access.internal;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.util.func.ArrayFunc;

public final class FunctionCompiler<F> {
	private final ArrayFunc<F> compiler;
	private final F empty;

	private List<F> functions;
	private F compiled;

	public FunctionCompiler(ArrayFunc<F> compiler, F empty) {
		this.compiler = compiler;
		this.empty = empty;
	}

	public void add(F function) {
		if (this.functions == null) {
			this.functions = new ArrayList<>();
		}
		this.functions.add(function);
		this.compiled = null;
	}

	public boolean isEmpty() {
		return this.functions == null;
	}

	public F get() {
		if (this.functions == null) {
			return this.empty;
		} else {
			F compiled = this.compiled;
			if(compiled == null) {
				this.compiled = compiled = this.compiler.combineList(this.functions);
			}
			return compiled;
		}
	}
}
