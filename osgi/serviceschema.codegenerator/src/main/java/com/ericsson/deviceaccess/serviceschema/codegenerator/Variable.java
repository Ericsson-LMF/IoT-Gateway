package com.ericsson.deviceaccess.serviceschema.codegenerator;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.JavaHelper.*;

/**
 *
 * @author delma
 */
public class Variable {

    private AccessModifier modifier;
    private final String name;
    private final String type;
    private String creationType;

    public Variable(String type, String name) {
        this.modifier = AccessModifier.PRIVATE;
        this.type = type;
        this.name = name;
    }

    public void setAccessModifier(AccessModifier modifier) {
        this.modifier = modifier;
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
        creationType = type;
        return this;
    }

    public String build(int indent) {
        StringBuilder result = new StringBuilder();
        String access = modifier.get();
        indent(result, indent).append(access).append(" ").append(type).append(" ").append(name);
        if (creationType != null) {
            //TODO: Chande intialization thingy so you can add parameters
            result.append(" = new ").append(creationType).append("(").append("").append(")");
        }
        return result.append(STATEMENT_END).toString();
    }

}
