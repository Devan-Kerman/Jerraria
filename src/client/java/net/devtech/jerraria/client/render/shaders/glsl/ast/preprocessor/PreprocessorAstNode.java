package net.devtech.jerraria.client.render.shaders.glsl.ast.preprocessor;

import net.devtech.jerraria.client.render.shaders.glsl.ast.AstNode;

/**
 * The node to present preprocessor in the code.
 *
 * @author JavaSaBr
 */
public class PreprocessorAstNode extends AstNode {

    /**
     * The type.
     */
    private String type;

    /**
     * Gets the type.
     *
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type.
     */
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    protected String getStringAttributes() {
        return getType();
    }
}
