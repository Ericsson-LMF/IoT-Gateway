package com.ericsson.deviceaccess.serviceschema.codegenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author delma
 */
public class Method {

    private final AccessModifier modifier;
    private final String name;
    private final String type;
    private final List<Param> parameters;
    private final List<String> lines;

    public Method(AccessModifier modifier, String type, String name) {
        this.modifier = modifier;
        this.type = type;
        this.name = name;
        parameters = new ArrayList<>();
        lines = new ArrayList<>();
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

    public Method addParameter(Param parameter) {
        parameters.add(parameter);
        return this;
    }

    public Method addLine(String line) {
        lines.add(line);
        return this;
    }

    public List<Param> getParameters() {
        return parameters;
    }

    public Iterable<String> getCodeLines() {
        return lines;
    }

}
