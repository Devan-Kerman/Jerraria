package net.devtech.jerraria.client.render.shaders.glsl.ast.declaration;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.client.render.shaders.glsl.ast.NameAstNode;
import net.devtech.jerraria.client.render.shaders.glsl.ast.TypeAstNode;

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
