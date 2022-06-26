package net.devtech.jerraria.render.api.batch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.element.AutoStrat;

public class StrategyKeys<T extends Shader<?>> {
	final Map<AutoStrat, ShaderKey<T>> keyMap = new ConcurrentHashMap<>();
	final Function<AutoStrat, ShaderKey<T>> keyGenerator;

	public StrategyKeys(Function<AutoStrat, ShaderKey<T>> generator) {
		this.keyGenerator = generator;
	}

	public StrategyKeys(T shader, Function<T, ShaderKey<T>> generator) {
		this(strat -> generator.apply(shader));
	}

	public ShaderKey<T> getFor(AutoStrat strat) {
		return this.keyMap.computeIfAbsent(strat, this.keyGenerator);
	}

	public T getBatch(BatchedRenderer renderer, AutoStrat strat) {
		return renderer.getBatch(this.getFor(strat));
	}
}
