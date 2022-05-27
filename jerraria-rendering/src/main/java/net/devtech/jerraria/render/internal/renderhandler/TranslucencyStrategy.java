package net.devtech.jerraria.render.internal.renderhandler;

import java.util.function.Supplier;

import net.devtech.jerraria.render.api.OpenGLSupport;
import net.devtech.jerraria.render.internal.renderhandler.translucent.AbstractTranslucencyRenderHandler;
import net.devtech.jerraria.render.internal.renderhandler.translucent.LinkedListTranslucentRenderer;
import net.devtech.jerraria.render.internal.renderhandler.translucent.DoublePassWeightedRenderer;
import net.devtech.jerraria.render.internal.renderhandler.translucent.WeightedTranslucentRenderer;

public enum TranslucencyStrategy {
	/**
	 * <b><a href="https://dl.acm.org/doi/10.1111/j.1467-8659.2010.01725.x">Original Research Paper</a></b> or
	 * <b><a href="https://sci-hub.hkvisa.net/10.1111/j.1467-8659.2010.01725.x">If you're some kind of commie bastard</a></b>
	 *
	 * <p>
	 *     Advantages: <br>
	 *      - 100% accurate <br>
	 *     Disadvantages: <br>
	 *     - A bit slow
	 *     - requires gl >4.3
	 * </p>
	 */
	LINKED_LIST("430"),

	/**
	 * <b><a href="https://jcgt.org/published/0002/02/09/">Original Paper</a></b>
	 * <p>
	 *     Advantages: <br>
	 *      - Fast <br>
	 *     Disadvantages: <br>
	 *     - Inaccurate, often ugly
	 *     - requires gl >4.0
	 * </p>
	 */
	SINGLE_PASS_WEIGHTED_BLENDED("330"),

	DOUBLE_PASS_WEIGHTED_BLENDED("330");

	public static final boolean SUPPORTS_LINKED_LIST = OpenGLSupport.ATOMIC_COUNTERS & OpenGLSupport.IMAGE_LOAD_SIZE & OpenGLSupport.IMAGE_LOAD_STORE;
	public static final boolean SUPPORTS_SINGLE_PASS_WEIGHTED_BLENDED = OpenGLSupport.BLEND_FUNC_I;

	public static final TranslucencyStrategy RECOMMENDED;

	static {
		if(SUPPORTS_LINKED_LIST) {
			RECOMMENDED = LINKED_LIST;
		} else if(SUPPORTS_SINGLE_PASS_WEIGHTED_BLENDED) {
			RECOMMENDED = SINGLE_PASS_WEIGHTED_BLENDED;
		} else {
			RECOMMENDED = DOUBLE_PASS_WEIGHTED_BLENDED;
		}
	}

	public static AbstractTranslucencyRenderHandler createTranslucentRenderer(TranslucencyStrategy strategy) {
		return switch(strategy) {
			case LINKED_LIST -> new LinkedListTranslucentRenderer();
			case SINGLE_PASS_WEIGHTED_BLENDED -> new WeightedTranslucentRenderer();
			case DOUBLE_PASS_WEIGHTED_BLENDED -> new DoublePassWeightedRenderer();
		};
	}

	public final String minGlslVersion;

	TranslucencyStrategy(String version) {
		this.minGlslVersion = version;
	}

	public <T> T calcIf(TranslucencyStrategy strategy, Supplier<T> value) {
		return strategy == this ? value.get() : null;
	}
}
