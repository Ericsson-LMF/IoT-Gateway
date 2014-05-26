package com.ericsson.deviceaccess.serviceschema.codegenerator;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Builder for building Javadoc comments
 *
 * @author delma
 */
final class JavadocBuilder {

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
    JavadocBuilder() {
        builder = new StringBuilder();
        builder.append(JAVADOC_START);
    }

    /**
     * New builder with object in it
     */
    JavadocBuilder(Object object) {
        this();
        line(object);
    }

    /**
     * Appends new line
     *
     * @param object to be appended
     * @return this
     */
    JavadocBuilder line(Object object) {
        builder.append(LINE_END).append(LINE_START).append(object);
        return this;
    }

    /**
     * Appends new line
     *
     * @param builder to be appended
     * @return this
     */
    JavadocBuilder line(JavadocBuilder builder) {
        builder.append(LINE_END).append(LINE_START).append(builder.builder);
        return this;
    }

    /**
     * Appends new empty line
     *
     * @return this
     */
    JavadocBuilder line() {
        builder.append(LINE_END).append(LINE_START);
        return this;
    }

    /**
     * Appends to the last line
     *
     * @param object to be appended
     * @return this
     */
    JavadocBuilder append(Object object) {
        builder.append(Objects.toString(object));
        return this;
    }

    /**
     * Appends to the last line
     *
     * @param builder to be appended
     * @return this
     */
    JavadocBuilder append(JavadocBuilder builder) {
        builder.append(Objects.toString(builder.builder));
        return this;
    }

    /**
     * Executes operator on this builder
     *
     * @param function in which editing current builder happends.
     * @return this
     */
    JavadocBuilder append(UnaryOperator<JavadocBuilder> function) {
        function.apply(this);
        return this;
    }

    /**
     * Tags Javadoc as being inherited from super class or interface
     *
     * @return this
     */
    JavadocBuilder inherit() {
        return line(TAG_INHERITED);
    }

    /**
     * Adds parameter tag to Javadoc
     *
     * @param parameter
     * @return this
     */
    JavadocBuilder parameter(Object name, Object description) {
        return line(TAG_PARAMETER).append(name).append(" ").append(description);
    }

    JavadocBuilder result(Object object) {
        return line(TAG_RETURN).append(object);
    }

    /**
     * Builds the Javadoc comment.
     *
     * @see toString does the same.
     * @return Javadoc comment in string form
     */
    String build() {
        return builder.append(LINE_END).append(JAVADOC_END).append(LINE_END).toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
