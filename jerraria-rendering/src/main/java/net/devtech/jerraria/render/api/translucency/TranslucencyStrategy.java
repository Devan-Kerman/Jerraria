package net.devtech.jerraria.render.api.translucency;

import java.util.function.Supplier;

import net.devtech.jerraria.render.api.OpenGLSupport;
import org.lwjgl.opengl.GL33;

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
	LINKED_LIST,

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
	SINGLE_PASS_WEIGHTED_BLENDED;

	public static final boolean SUPPORTS_LINKED_LIST = OpenGLSupport.ATOMIC_COUNTERS & OpenGLSupport.IMAGE_LOAD_SIZE & OpenGLSupport.IMAGE_LOAD_STORE;

	public static final TranslucencyStrategy RECOMMENDED = SUPPORTS_LINKED_LIST ? LINKED_LIST : SINGLE_PASS_WEIGHTED_BLENDED;

	public <T> T calcIf(TranslucencyStrategy strategy, Supplier<T> value) {
		return strategy == this ? value.get() : null;
	}
}
