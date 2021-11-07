package net.devtech.jerraria.client.render.glsl.ast.declaration;

import net.devtech.jerraria.client.render.glsl.ast.NameAstNode;
import net.devtech.jerraria.client.render.glsl.ast.TypeAstNode;

public class StructDeclarationAstNode extends TypeAstNode {
	private NameAstNode nameNode;

	public StructDeclarationAstNode() {}

	public NameAstNode getNameNode() {
		return this.nameNode;
	}

	public StructDeclarationAstNode setNameNode(NameAstNode nameNode) {
		this.nameNode = nameNode;
		setName(nameNode.getName());
		return this;
	}
}
