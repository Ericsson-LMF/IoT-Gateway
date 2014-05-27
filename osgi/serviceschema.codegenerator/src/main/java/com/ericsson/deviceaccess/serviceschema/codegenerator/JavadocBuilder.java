package com.ericsson.deviceaccess.serviceschema.codegenerator;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Builder for building Javadoc comments
 *
 * @author delma
 */
public final class JavadocBuilder {

    private StringBuilder builder;
    private static final String JAVADOC_START = "/**";
    private static final String JAVADOC_END = " */";
    private static final String LINE_START = " * ";
    private static final String LINE_END = "\n";
    private static final String TAG_INHERITED = "{@inheritDoc}";
    private static final String TAG_PARAMETER = "@param ";
    private static final String TAG_RETURN = "@return ";

    /**
     * New builder
     */
    public JavadocBuilder() {
        builder = new StringBuilder();
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
            builder.append(LINE_END).append(LINE_START).append(object);
        }
        return this;
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
        }
        return this;
    }

    /**
     * Appends new empty line
     *
     * @return this
     */
    public JavadocBuilder line() {
        builder.append(LINE_END).append(LINE_START);
        return this;
    }

    /**
     * Appends to the last line
     *
     * @param object to be appended
     * @return this
     */
    public JavadocBuilder append(Object object) {
        if (object != null) {
            builder.append(object.toString());
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
        return line(TAG_INHERITED);
    }

    /**
     * Adds parameter tag to Javadoc
     *
     * @param name
     * @param description
     * @return this
     */
    public JavadocBuilder parameter(Object name, Object description) {
        return line(TAG_PARAMETER).append(name).append(" ").append(description);
    }

    public JavadocBuilder result(Object object) {
        return line(TAG_RETURN).append(object);
    }

    /**
     * Builds the Javadoc comment.
     *
     * @see toString does the same.
     * @return Javadoc comment in string form
     */
    public String build() {
        return JAVADOC_START + builder.toString() + LINE_END + JAVADOC_END + LINE_END;
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
}
