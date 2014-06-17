package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Builder of Javadoc
 *
 * @author delma
 */
public final class Javadoc {

    private static final String JAVADOC_START = "/**";
    private static final String JAVADOC_END = " */";
    private static final String LINE_START = " * ";
    private static final String LINE_END = "\n";

    private StringBuilder builder;
    private Map<Tag, List<String>> tags;

    /**
     * New Javadoc builder
     */
    public Javadoc() {
        builder = new StringBuilder();
        tags = new EnumMap<>(Tag.class);
    }

    /**
     * New Javadoc builder with object in it
     *
     * @param object
     */
    public Javadoc(Object object) {
        this();
        line(object);
    }

    /**
     * New Javadoc builder with existing Javadoc builder in it
     *
     * @param builder
     */
    public Javadoc(Javadoc builder) {
        this();
        append(builder);
    }

    /**
     * Adds new line
     *
     * @param object to be appended
     * @return this
     */
    public Javadoc line(Object object) {
        if (object != null) {
            line(builder, object);
        }
        return this;
    }

    private StringBuilder line(StringBuilder builder, Object object) {
        return builder.append(LINE_END).append(LINE_START).append(object);
    }

    /**
     * Adds Javadoc to new line
     *
     * @param javadoc to be appended
     * @return this
     */
    public Javadoc line(Javadoc javadoc) {
        if (javadoc != null) {
            line(javadoc.builder);
            tags.putAll(javadoc.tags);
        }
        return this;
    }

    /**
     * Adds new empty line
     *
     * @return this
     */
    public Javadoc emptyLine() {
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
    public Javadoc append(Object object) {
        if (object != null) {
            builder.append(object);
        }
        return this;
    }

    /**
     * Appends Javadoc to the last line
     *
     * @param javadoc to be appended
     * @return this
     */
    public Javadoc append(Javadoc javadoc) {
        if (javadoc != null) {
            builder.append(javadoc.builder);
            tags.putAll(javadoc.tags);
        }
        return this;
    }

    /**
     * Executes operator on this builder
     *
     * @param function in which editing current builder happens.
     * @return this
     */
    public Javadoc append(UnaryOperator<Javadoc> function) {
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
    public Javadoc inherit() {
        addTag(Tag.INHERITED, "");
        return this;
    }

    /**
     * Adds parameter tag to Javadoc
     *
     * @param name
     * @param description
     * @return this
     */
    public Javadoc parameter(Object name, Object description) {
        addTag(Tag.PARAMETER, name + " " + description);
        return this;
    }

    /**
     * Adds exception tag to Javadoc
     *
     * @param name
     * @param description
     * @return this
     */
    public Javadoc exception(Object name, Object description) {
        addTag(Tag.THROW, name + " " + description);
        return this;
    }

    /**
     * Adds return tag to Javadoc
     *
     * @param object description
     * @return this
     */
    public Javadoc result(Object object) {
        addTag(Tag.RETURN, object);
        return this;
    }

    /**
     * Adds tag to Javadoc
     *
     * @param tag
     * @param value
     * @return this
     */
    private Javadoc addTag(Tag tag, Object value) {
        tags.putIfAbsent(tag, new ArrayList<>());
        tags.get(tag).add(value.toString());
        return this;
    }

    /**
     * Builds the Javadoc comment.
     *
     * @see toString does the same.
     * @return Javadoc comment in string form
     */
    public String build() {
        return JAVADOC_START + innerBuild() + LINE_END + JAVADOC_END + LINE_END;
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
    protected String build(int indent) {
        StringBuilder result = new StringBuilder();
        for (String line : build().split(LINE_END)) {
            JavaHelper.indent(result, indent).append(line).append(LINE_END);
        }
        return result.toString();
    }

    private String innerBuild() {
        StringBuilder result = new StringBuilder(builder);
        if (!tags.isEmpty()) {
            emptyLine(result);
        }
        tags.forEach((k, v) -> {
            v.forEach(s -> line(result, k.get()).append(" ").append(s));
        });
        return result.toString();
    }

    private enum Tag {

        INHERITED("{@inheritDoc}"),
        PARAMETER("@param"),
        RETURN("@return"),
        THROW("@throws");
        private final String string;

        Tag(String string) {
            this.string = string;
        }

        public String get() {
            return string;
        }
    }
}
