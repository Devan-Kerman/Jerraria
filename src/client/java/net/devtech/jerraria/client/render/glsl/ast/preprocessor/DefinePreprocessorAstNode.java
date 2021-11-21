package net.devtech.jerraria.client.render.glsl.ast.preprocessor;

import net.devtech.jerraria.client.render.glsl.GlslLang;
import net.devtech.jerraria.client.render.glsl.ast.NameAstNode;
import net.devtech.jerraria.client.render.glsl.ast.value.ValueAstNode;

/**
 * The node to present stack define preprocessor in the code.
 *
 * @author JavaSaBr
 */
public class DefinePreprocessorAstNode extends PreprocessorAstNode {

    /**
     * The definition name.
     */
    private NameAstNode name;

    /**
     * The definition value.
     */
    private ValueAstNode value;

    /**
     * Gets the name of this definition.
     *
     * @return the name of this definition.
     */
    public NameAstNode getName() {
        return name;
    }

    /**
     * Sets the name of this definition.
     *
     * @param name the name of this definition.
     */
    public void setName(final NameAstNode name) {
        this.name = name;
    }

    /**
     * Gets the value of this definition.
     *
     * @return the value of this definition.
     */
    public ValueAstNode getValue() {
        return value;
    }

    /**
     * Sets the value of this definition.
     *
     * @param value the value of this definition.
     */
    public void setValue(final ValueAstNode value) {
        this.value = value;
    }

    @Override
    protected String getStringAttributes() {
        return GlslLang.PR_TYPE_DEFINE;
    }
}
