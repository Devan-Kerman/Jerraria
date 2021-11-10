package net.devtech.jerraria.client.render.glsl.ast;

/**
 * The node to present a symbol.
 *
 * @author JavaSaBr
 */
public class SymbolAstNode extends AstNode {

    @Override
    protected String getStringAttributes() {
        return getText();
    }
}