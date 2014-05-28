package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.*;

/**
 *
 * @author delma
 */
public class Variable {

    private AccessModifier modifier;
    private final String name;
    private final String type;
    private String initCode;

    public Variable(String type, String name) {
        this.modifier = AccessModifier.PRIVATE;
        this.type = type;
        this.name = name;
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
        StringBuilder result = new StringBuilder();
        String access = modifier.get();
        indent(result, indent).append(access).append(" ").append(type).append(" ").append(name);
        if (initCode != null) {
            result.append(" = ").append(initCode);
        }
        return result.append(STATEMENT_END).append(LINE_END).toString();
    }

}
