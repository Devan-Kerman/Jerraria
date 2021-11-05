package net.devtech.jerraria.client.render.shaders.glsl.ast.branching.condition;

import net.devtech.jerraria.client.render.shaders.glsl.GlslLang;

/**
 * The node to present define condition Is.
 *
 * @author JavaSaBr
 */
public class DefineConditionIsAstNode extends ConditionIsAstNode {

    @Override
    protected String getStringAttributes() {
        return GlslLang.PR_TYPE_DEFINE;
    }
}
