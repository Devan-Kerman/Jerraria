package net.devtech.jerraria.client.render.glsl.ast;

/**
 * The node to present stack symbol.
 *
 * @author JavaSaBr
 */
public class SymbolAstNode extends AstNode {

    @Override
    protected String getStringAttributes() {
        return getText();
    }
}
