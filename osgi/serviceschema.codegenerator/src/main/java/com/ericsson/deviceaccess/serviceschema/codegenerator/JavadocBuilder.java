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
        builder.append(JAVADOC_START);
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
     * @param builder to be appended
     * @return this
     */
    public JavadocBuilder line(JavadocBuilder builder) {
        if (builder != null) {
            builder.append(LINE_END).append(LINE_START).append(builder.builder);
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
     * @param builder to be appended
     * @return this
     */
    public JavadocBuilder append(JavadocBuilder builder) {
        if (builder != null) {
            builder.append(Objects.toString(builder.builder));
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
        return builder.append(LINE_END).append(JAVADOC_END).append(LINE_END).toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
