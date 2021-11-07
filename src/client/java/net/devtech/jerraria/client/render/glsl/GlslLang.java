package net.devtech.jerraria.client.render.glsl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The class with language constants.
 *
 * @author JavaSaBr
 */
public class GlslLang {
	public static final Set<String> GLSL_TYPES = new HashSet<>();
	public static final String KW_STRUCT = "struct";
	public static final String KW_LAYOUT = "layout";

	static {
		GLSL_TYPES.add("bool");
		GLSL_TYPES.add("uint");
		GLSL_TYPES.add("int");
		GLSL_TYPES.add("float");
		GLSL_TYPES.add("double");

		for(String prefix : List.of("b", "i", "u", "", "d")) {
			for(int i = 2; i <= 4; i++) {
				GLSL_TYPES.add(prefix + "vec" + i);
			}
		}

		for(int x = 2; x <= 4; x++) {
			GLSL_TYPES.add("mat" + x);
			for(int y = 2; y <= 4; y++) {
				GLSL_TYPES.add("mat" + x + "x" + y);
			}
		}
		GLSL_TYPES.add("sampler1D");
		GLSL_TYPES.add("sampler2D");
		GLSL_TYPES.add("sampler3D");
		GLSL_TYPES.add("samplerCube");
		GLSL_TYPES.add("sampler2DRect");
		GLSL_TYPES.add("sampler1DArray");
		GLSL_TYPES.add("sampler2DArray");
		GLSL_TYPES.add("samplerCubeArray");
		GLSL_TYPES.add("samplerBuffer");
		GLSL_TYPES.add("sampler2DMS");
		GLSL_TYPES.add("sampler2DMSArray");

		GLSL_TYPES.add("sampler1DShadow");
		GLSL_TYPES.add("sampler2DShadow");
		GLSL_TYPES.add("samplerCubeShadow");
		GLSL_TYPES.add("sampler2DRectShadow");
		GLSL_TYPES.add("sampler1DArrayShadow");
		GLSL_TYPES.add("sampler2DArrayShadow");
		GLSL_TYPES.add("samplerCubeArrayShadow");

		// todo atomics and images pain
	}

    public static final String PR_TYPE_IF = "if";
    public static final String PR_TYPE_IFDEF = "ifdef";
    public static final String PR_TYPE_IFNDEF = "ifndef";
    public static final String PR_TYPE_ELIF = "elif";

    public static final String PR_TYPE_DEFINE = "define";
    public static final String PR_TYPE_UNDEF = "undef";
    public static final String PR_TYPE_ELSE = "else";
    public static final String PR_TYPE_ENDIF = "endif";
    public static final String PR_TYPE_ERROR = "error";
    public static final String PR_TYPE_PRAGMA = "pragma";
    public static final String PR_TYPE_EXTENSION = "extension";
    public static final String PR_TYPE_IMPORT = "import";
    public static final String PR_TYPE_VERSION = "version";
    public static final String PR_TYPE_LINE = "line";


    public static final String KW_IF = "if";
    public static final String KW_ELSE = "else";
    public static final String KW_DISCARD = "discard";
    public static final String KW_FOR = "for";

    public static final Set<String> KEYWORDS = new HashSet<>();

    static {
        KEYWORDS.add("uniform");
        KEYWORDS.add("in");
        KEYWORDS.add("out");
        KEYWORDS.add("varying");
        KEYWORDS.add("attribute");
        KEYWORDS.add("discard");
        KEYWORDS.add("if");
        KEYWORDS.add("elif");
        KEYWORDS.add("endif");
        KEYWORDS.add("defined");
        KEYWORDS.add("define");
        KEYWORDS.add("else");
        KEYWORDS.add("ifdef");
        KEYWORDS.add("ifndef");
        KEYWORDS.add("const");
        KEYWORDS.add("break");
        KEYWORDS.add("continue");
        KEYWORDS.add("do");
        KEYWORDS.add("for");
        KEYWORDS.add("while");
        KEYWORDS.add("inout");
        KEYWORDS.add("struct");
        KEYWORDS.add("import");
		KEYWORDS.add("buffer");
	    KEYWORDS.add("layout");
		KEYWORDS.add("version");
    }

    public static final Set<String> PREPROCESSOR = new HashSet<>();
    public static final Set<String> PREPROCESSOR_WITH_CONDITION = new HashSet<>();

    static {
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_IF);
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_IFDEF);
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_IFNDEF);
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_ELIF);

        PREPROCESSOR.addAll(PREPROCESSOR_WITH_CONDITION);
        PREPROCESSOR.add(PR_TYPE_DEFINE);
        PREPROCESSOR.add(PR_TYPE_UNDEF);
        PREPROCESSOR.add(PR_TYPE_ELSE);
        PREPROCESSOR.add(PR_TYPE_ENDIF);
        PREPROCESSOR.add(PR_TYPE_ERROR);
        PREPROCESSOR.add(PR_TYPE_PRAGMA);
        PREPROCESSOR.add(PR_TYPE_EXTENSION);
        PREPROCESSOR.add(PR_TYPE_VERSION);
        PREPROCESSOR.add(PR_TYPE_LINE);
    }
}
