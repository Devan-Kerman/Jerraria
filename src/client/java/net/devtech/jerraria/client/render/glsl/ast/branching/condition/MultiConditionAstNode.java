package net.devtech.jerraria.client.render.glsl.ast.branching.condition;

import net.devtech.jerraria.client.render.glsl.ast.AstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The node to present stack multi expressions condition in the code.
 *
 * @author JavaSaBr
 */
public class MultiConditionAstNode extends ConditionAstNode {

    /**
     * The list of expressions.
     */
    private List<AstNode> expressions;

    protected MultiConditionAstNode() {
        this.expressions = new ArrayList<>();
    }

    /**
     * Gets the list of expressions.
     *
     * @return the list of expressions.
     */
    public List<AstNode> getExpressions() {
        return expressions;
    }

    /**
     * Adds the expression.
     *
     * @param expression the expression.
     */
    public void addExpression(final AstNode expression) {
        this.expressions.add(expression);
    }
}
