package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Builder for building Javadoc comments
 *
 * @author delma
 */
public final class JavadocBuilder {

    private StringBuilder builder;
    private Map<String, List<String>> tags;
    private static final String JAVADOC_START = "/**";
    private static final String JAVADOC_END = " */";
    private static final String LINE_START = " * ";
    private static final String LINE_END = "\n";
    private static final String TAG_INHERITED = "{@inheritDoc}";
    private static final String TAG_PARAMETER = "@param";
    private static final String TAG_RETURN = "@return";

    /**
     * New builder
     */
    public JavadocBuilder() {
        builder = new StringBuilder();
        tags = new HashMap<>();
    }

    /**
     * New builder with object in it
     *
     * @param object
     */
    public JavadocBuilder(Object object) {
        this();
        line(object);
    }

    /**
     * New builder with existing builder in it
     *
     * @param builder
     */
    public JavadocBuilder(JavadocBuilder builder) {
        this();
        append(builder);
    }

    /**
     * Appends new line
     *
     * @param object to be appended
     * @return this
     */
    public JavadocBuilder line(Object object) {
        if (object != null) {
            line(builder, object);
        }
        return this;
    }

    private StringBuilder line(StringBuilder builder, Object object) {
        return builder.append(LINE_END).append(LINE_START).append(object);
    }

    /**
     * Appends new line
     *
     * @param javadoc to be appended
     * @return this
     */
    public JavadocBuilder line(JavadocBuilder javadoc) {
        if (javadoc != null) {
            line(javadoc.builder);
            tags.putAll(javadoc.tags);
        }
        return this;
    }

    /**
     * Appends new empty line
     *
     * @return this
     */
    public JavadocBuilder emptyLine() {
        emptyLine(builder);
        return this;
    }

    private StringBuilder emptyLine(StringBuilder builder) {
        return line(builder, "");
    }

    /**
     * Appends to the last line
     *
     * @param object to be appended
     * @return this
     */
    public JavadocBuilder append(Object object) {
        if (object != null) {
            builder.append(object);
        }
        return this;
    }

    /**
     * Appends to the last line
     *
     * @param javadoc to be appended
     * @return this
     */
    public JavadocBuilder append(JavadocBuilder javadoc) {
        if (javadoc != null) {
            builder.append(javadoc.builder);
            tags.putAll(javadoc.tags);
        }
        return this;
    }

    /**
     * Executes operator on this builder
     *
     * @param function in which editing current builder happends.
     * @return this
     */
    public JavadocBuilder append(UnaryOperator<JavadocBuilder> function) {
        if (function != null) {
            function.apply(this);
        }
        return this;
    }

    /**
     * Tags Javadoc as being inherited from super class or interface
     *
     * @return this
     */
    public JavadocBuilder inherit() {
        addTag(TAG_INHERITED, "");
        return this;
    }

    /**
     * Adds parameter tag to Javadoc
     *
     * @param name
     * @param description
     * @return this
     */
    public JavadocBuilder parameter(Object name, Object description) {
        addTag(TAG_PARAMETER, name + " " + description);
        return this;
    }

    public JavadocBuilder result(Object object) {
        addTag(TAG_RETURN, object);
        return this;
    }

    private void addTag(String tag, Object value) {
        tags.putIfAbsent(tag, new ArrayList<>());
        tags.get(tag).add(value.toString());
    }

    /**
     * Builds the Javadoc comment.
     *
     * @see toString does the same.
     * @return Javadoc comment in string form
     */
    public String build() {
        return JAVADOC_START + inBuild() + LINE_END + JAVADOC_END + LINE_END;
    }

    @Override
    public String toString() {
        return build();
    }

    /**
     * Builds the Javadoc comment with specified indent.
     *
     * @param indent
     * @return Javadoc comment in string form
     */
    String build(int indent) {
        StringBuilder result = new StringBuilder();
        for (String line : build().split(LINE_END)) {
            JavaHelper.indent(result, indent).append(line).append(LINE_END);
        }
        return result.toString();
    }

    private String inBuild() {
        StringBuilder result = new StringBuilder(builder);
        if (!tags.isEmpty()) {
            emptyLine(result);
        }
        tags.forEach((k, v) -> {
            v.forEach(s -> line(result, k).append(" ").append(s));
        });
        return result.toString();
    }
}
