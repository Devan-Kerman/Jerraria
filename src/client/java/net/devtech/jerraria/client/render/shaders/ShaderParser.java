package net.devtech.jerraria.client.render.shaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.client.render.shaders.glsl.GlslLang;
import net.devtech.jerraria.client.render.shaders.glsl.GlslParser;
import net.devtech.jerraria.client.render.shaders.glsl.ast.AstNode;
import net.devtech.jerraria.client.render.shaders.glsl.ast.declaration.ExternalFieldDeclarationAstNode;
import net.devtech.jerraria.client.render.shaders.glsl.ast.declaration.FieldDeclarationAstNode;
import net.devtech.jerraria.client.render.shaders.glsl.ast.declaration.FileDeclarationAstNode;
import net.devtech.jerraria.client.render.shaders.glsl.ast.declaration.StructDeclarationAstNode;
import org.intellij.lang.annotations.Language;
import org.lwjgl.opengl.GL20;

public class ShaderParser {
	final GlslParser parser = GlslParser.newInstance();
	final FileDeclarationAstNode node;
	final String sourceCode;

	public ShaderParser(@Language("GLSL") String code) {
		this.sourceCode = code;
		this.node = parser.parseFileDeclaration("", code);
	}

	public static void main(String[] args) {
		ShaderParser parser = new ShaderParser("""
            struct st_ructure {
                vec4 fieldA;
                vec3 fieldB;
                int i;
            };
            uniform st_ructure val;
            uniform vec3 hello[4];
            layout(location = 0) in int i;
			""".stripIndent());
		System.out.println(parser.getAttributeNames(ExternalFieldDeclarationAstNode.ExternalFieldType.ATTRIBUTE));
		System.out.println(parser.getAttributeNames(ExternalFieldDeclarationAstNode.ExternalFieldType.UNIFORM));
	}


	public List<String> getAttributeNames(ExternalFieldDeclarationAstNode.ExternalFieldType type) {
		List<String> list = new ArrayList<>();
		Map<String, List<FieldDeclarationAstNode>> structures = new HashMap<>();
		for(AstNode child : node.getChildren()) {
			if(child instanceof ExternalFieldDeclarationAstNode a && a.getFieldType() == type) {
				applyField(list, structures, "", a);
			} else if(child instanceof StructDeclarationAstNode s) {
				List<FieldDeclarationAstNode> nodes = new ArrayList<>();
				for(AstNode sChild : s.getChildren()) {
					if(sChild instanceof FieldDeclarationAstNode f) {
						nodes.add(f);
					}
				}
				structures.put(s.getName(), nodes);
			}
		}
		return list;
	}

	private static void applyField(List<String> list,
		Map<String, List<FieldDeclarationAstNode>> structures,
		String prefix,
		FieldDeclarationAstNode field) {
		String name = field.getName().getName();
		String typeName = field.getType().getName();
		if(GlslLang.GLSL_TYPES.contains(typeName)) {
			list.add(prefix + name);
		} else {
			getAttributeNames(structures, list, name + ".", typeName);
		}
	}

	private static void getAttributeNames(Map<String, List<FieldDeclarationAstNode>> structures,
		List<String> list,
		String prefix,
		String structureName) {
		for(FieldDeclarationAstNode node : structures.get(structureName)) {
			applyField(list, structures, prefix, node);
		}
	}
}
