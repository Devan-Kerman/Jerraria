package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glGetShaderSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParserBaseVisitor;
import io.github.douira.glsl_transformer.transform.JobParameters;
import io.github.douira.glsl_transformer.transform.TransformationManager;
import io.github.douira.glsl_transformer.tree.ExtendedContext;

public class GLSLParserValidation {
	public static void validateFragShader(int program, BareShader.Uncompiled uncompiled) {
		String fragSource = glGetShaderSource(program);
		TransformationManager<JobParameters> transform = new TransformationManager<>();
		var parse = transform.parse(fragSource, GLSLParser::translationUnit);
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
		Map<String, BareShader.Field> fields = uncompiled.outputFields;
		Set<String> unreferencedNames = new HashSet<>(names);
		unreferencedNames.removeAll(fields.keySet());
		Set<String> unresolvedName = new HashSet<>(fields.keySet());
		names.forEach(unresolvedName::remove);

		if(!unreferencedNames.isEmpty() || !unresolvedName.isEmpty()) {
			throw new UnsupportedOperationException("Unable to find output(s) " + unresolvedName + ", outputs with name " + unreferencedNames + " were not referenced!");
		}


	}
}
