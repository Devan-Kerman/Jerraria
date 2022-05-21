package rendering;

import java.util.ArrayList;
import java.util.List;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParserBaseVisitor;
import io.github.douira.glsl_transformer.transform.JobParameters;
import io.github.douira.glsl_transformer.transform.TransformationManager;
import io.github.douira.glsl_transformer.tree.ExtendedContext;

public class ParserTest {
	public static void main(String[] args) {
		TransformationManager<JobParameters> transform = new TransformationManager<>();
		var parse = transform.parse("""
			#version 330 core

			in vec3 oPos;
			in vec4 oColor;

			out vec4 accum;
			out float reveal;

			void main(in vec4 test) {
				vec4 color = oColor;
				float weight = max(min(1.0, max(max(color.r, color.g), color.b) * color.a), color.a) * clamp(0.03 / (1e-5 + pow(oPos.z / 200, 4.0)), 1e-2, 3e3);
				accum = vec4(color.rgb * color.a, color.a) * weight;
				reveal = color.a;
			}
			""", GLSLParser::translationUnit);

		List<String> names = new ArrayList<>();
		parse.accept(new GLSLParserBaseVisitor<>() {
			@Override
			public Object visitStorageQualifier(GLSLParser.StorageQualifierContext ctx) {
				Object o = super.visitStorageQualifier(ctx);
				if(ctx.getToken(GLSLParser.OUT, 0) == null) {
					return o;
				}

				GLSLParser.InitDeclaratorListContext parent = null;
				for(ExtendedContext ext = ctx; ext != null; ext = ext.getParent()) {
					if(ext instanceof GLSLParser.InitDeclaratorListContext e) {
						parent = e;
						break;
					}
				}
				if(parent != null) {
					for(GLSLParser.DeclarationMemberContext member : parent.declarationMembers) {
						names.add(member.getText());
					}
				}
				return o;
			}
		});
		System.out.println(names);
	}
}
