package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import java.util.regex.Pattern;

/**
 *
 * @author delma
 */
public enum JavaHelper {

    INSTANSE;
    public static final String INDENT = "    ";
    public static final String LINE_END = "\n";
    public static final String STATEMENT_END = ";";
    public static final String BLOCK_START = "{";
    public static final String BLOCK_END = "}";
    public static final String REPLACEMENT_START = "#";
    public static final String REPLACEMENT_END = ";";
    public static final String PACKAGE = "package";
    public static final String IMPORT = "import";
    private static final String ENDFUL_PATTERN = REPLACEMENT_START + "\\d*" + REPLACEMENT_END;
    private static final String ENDLESS_PATTERN = REPLACEMENT_START + "\\d+";
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("(" + ENDFUL_PATTERN + ")|(" + ENDLESS_PATTERN + ")");

    public static String getGenerationWarning(Class<?> generator, Class<?> caller) {
        return "THIS IS AUTOMATICALLY GENERATED BY {@link " + generator.getCanonicalName() + "}.";
    }

    public static StringBuilder indent(StringBuilder builder, int indent) {
        while (0 < indent) {
            builder.append(INDENT);
            indent--;
        }
        return builder;
    }

    public static StringBuilder emptyLine(StringBuilder builder) {
        return builder.append(LINE_END);
    }

}
