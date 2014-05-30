package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.STATEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;
import java.util.EnumSet;

/**
 *
 * @author delma
 */
public class Variable {

    private AccessModifier modifier;
    private final String name;
    private final String type;
    private String initCode;
    private final EnumSet<OptionalModifier> modifiers;
    private Javadoc javadoc;

    public Variable(String type, String name) {
        this.modifier = AccessModifier.PRIVATE;
        this.type = type;
        this.name = name;
        modifiers = EnumSet.noneOf(OptionalModifier.class);
    }

    public Variable setAccessModifier(AccessModifier modifier) {
        this.modifier = modifier;
        return this;
    }

    public AccessModifier getAccessModifier() {
        return modifier;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Variable init(String type) {
        initCode = type;
        return this;
    }

    public String build(int indent) {
        StringBuilder builder = new StringBuilder();
        String access = modifier.get();
        if (javadoc != null) {
            builder.append(javadoc.build(indent));
        }
        indent(builder, indent).append(access).append(" ");
        modifiers.forEach(m -> builder.append(m.get()).append(" "));
        builder.append(type).append(" ").append(name);
        if (initCode != null) {
            builder.append(" = ").append(initCode);
        }
        return builder.append(STATEMENT_END).append(LINE_END).toString();
    }

    public Variable addModifier(OptionalModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    public Variable setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }
}
