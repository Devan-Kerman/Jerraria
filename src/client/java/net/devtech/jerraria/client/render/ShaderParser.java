package net.devtech.jerraria.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.client.render.glsl.GlslLang;
import net.devtech.jerraria.client.render.glsl.GlslParser;
import net.devtech.jerraria.client.render.glsl.ast.AstNode;
import net.devtech.jerraria.client.render.glsl.ast.NameAstNode;
import net.devtech.jerraria.client.render.glsl.ast.TypeAstNode;
import net.devtech.jerraria.client.render.glsl.ast.declaration.ExternalFieldDeclarationAstNode;
import net.devtech.jerraria.client.render.glsl.ast.declaration.FieldDeclarationAstNode;
import net.devtech.jerraria.client.render.glsl.ast.declaration.FileDeclarationAstNode;
import net.devtech.jerraria.client.render.glsl.ast.declaration.StructDeclarationAstNode;
import org.intellij.lang.annotations.Language;

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
                ushort fieldA;
                vec3 fieldB;
                int i;
            };
            uniform st_ructure val;
            uniform vec3 hello[4];
            layout(location = 0) in int i[4];
			""".stripIndent());
		System.out.println(parser.getFields(ExternalFieldDeclarationAstNode.ExternalFieldType.ATTRIBUTE));
		System.out.println(parser.getFields(ExternalFieldDeclarationAstNode.ExternalFieldType.UNIFORM));
	}

	public record Field(String name, String glslType) {}

	public List<Field> getFields(ExternalFieldDeclarationAstNode.ExternalFieldType type) {
		List<Field> list = new ArrayList<>();
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

	private static void applyField(List<Field> list,
		Map<String, List<FieldDeclarationAstNode>> structures,
		String prefix,
		FieldDeclarationAstNode field) {
		NameAstNode astName = field.getName();
		String name = astName == null ? "" : astName.getName();
		TypeAstNode type = field.getType();
		String typeName = type.getName();
		if(GlslLang.GLSL_TYPES.contains(typeName)) {
			list.add(new Field(prefix + name, typeName));
		} else {
			getAttributeNames(structures, list, astName == null ? "" : name + ".", typeName);
		}
	}

	private static void getAttributeNames(Map<String, List<FieldDeclarationAstNode>> structures,
		List<Field> list,
		String prefix,
		String structureName) {
		System.out.println(structureName);
		for(FieldDeclarationAstNode node : structures.get(structureName)) {
			applyField(list, structures, prefix, node);
		}
	}
}
