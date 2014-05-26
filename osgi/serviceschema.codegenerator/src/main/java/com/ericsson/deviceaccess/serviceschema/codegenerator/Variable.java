package com.ericsson.deviceaccess.serviceschema.codegenerator;

/**
 *
 * @author delma
 */
public class Variable {

    private final AccessModifier modifier;
    private final String name;
    private final String type;

    public Variable(AccessModifier modifier, String type, String name) {
        this.modifier = modifier;
        this.type = type;
        this.name = name;
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

}
