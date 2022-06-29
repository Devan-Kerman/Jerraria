package net.devtech.jerraria.render.api.batch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.element.AutoStrat;

public class StrategyKeys<T extends Shader<?>> {
	final Map<AutoStrat, BasicShaderKey<T>> keyMap = new ConcurrentHashMap<>();
	final T shader;
	public StrategyKeys(T shader) {
		this.shader = shader;
	}

	public BasicShaderKey<T> getFor(AutoStrat strat) {
		return this.keyMap.computeIfAbsent(strat, strat1 -> BasicShaderKey.key(this.shader).withPreConfig(shader1 -> shader1.strategy(strat1)));
	}

	public T getBatch(BatchedRenderer renderer, AutoStrat strat) {
		return renderer.getBatch(this.getFor(strat));
	}
}
