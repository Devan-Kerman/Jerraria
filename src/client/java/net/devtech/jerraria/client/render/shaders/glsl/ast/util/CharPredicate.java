package net.devtech.jerraria.client.render.shaders.glsl.ast.util;

/**
 * The predicate interface.
 *
 * @author JavaSaBr
 */
public interface CharPredicate {

    /**
     * Tests the value.
     *
     * @param value the value.
     * @return true if test was successful.
     */
    boolean test(char value);
}
