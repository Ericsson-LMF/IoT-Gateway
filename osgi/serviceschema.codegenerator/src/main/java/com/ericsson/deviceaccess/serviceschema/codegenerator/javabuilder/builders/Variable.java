package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Component;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Modifierable;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.STATEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;
import java.util.EnumSet;

/**
 * Variable of {@link JavaClass}
 *
 * @author delma
 */
public class Variable implements Component, Modifierable {

    private AccessModifier modifier;
    private final String name;
    private final String type;
    private String initCode;
    private final EnumSet<OptionalModifier> modifiers;
    private Javadoc javadoc;

    /**
     * Creates new Variable with type and name
     *
     * @param type
     * @param name
     */
    public Variable(String type, String name) {
        this.modifier = AccessModifier.PRIVATE;
        this.type = type;
        this.name = name;
        modifiers = EnumSet.noneOf(OptionalModifier.class);
    }

    @Override
    public Variable setAccessModifier(AccessModifier modifier) {
        this.modifier = modifier;
        return this;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return modifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets how variable is initialized
     *
     * @param code initialization code
     * @return this
     */
    public Variable init(String code) {
        initCode = code;
        return this;
    }

    /**
     * Builds Variable to string with specified indent
     *
     * @param indent how much indent there is
     * @return builded string
     */
    public String build(int indent) {
        StringBuilder builder = new StringBuilder();
        String access = modifier.get();
        addJavadoc(builder, indent);
        indent(builder, indent).append(access).append(" ");
        modifiers.forEach(m -> builder.append(m.get()).append(" "));
        builder.append(type).append(" ").append(name);
        addInitialization(builder);
        builder.append(STATEMENT_END).append(LINE_END);
        return builder.toString();
    }

    private void addInitialization(StringBuilder builder) {
        if (initCode != null) {
            builder.append(" = ").append(initCode);
        }
    }

    private void addJavadoc(StringBuilder builder, int indent) {
        if (javadoc != null) {
            builder.append(javadoc.build(indent));
        }
    }

    @Override
    public Variable addModifier(OptionalModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    @Override
    public Variable setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }
}
