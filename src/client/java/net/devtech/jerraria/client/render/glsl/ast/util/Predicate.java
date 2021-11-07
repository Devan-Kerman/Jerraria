package net.devtech.jerraria.client.render.glsl.ast.util;

/**
 * The predicate interface.
 *
 * @author JavaSaBr
 */
public interface Predicate<T> {

    /**
     * Tests the value.
     *
     * @param value the value.
     * @return true if test was successful.
     */
    boolean test(T value);
}
