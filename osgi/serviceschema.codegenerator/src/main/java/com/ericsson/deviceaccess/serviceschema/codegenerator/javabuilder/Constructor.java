package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.*;

/**
 *
 * @author delma
 */
public class Constructor implements CodeBlock {

    private AccessModifier accessModifier;
    private final List<Param> parameters;
    private final List<String> lines;
    private JavadocBuilder javadoc;
    private JavaBuilder owner;

    public Constructor() {
        parameters = new ArrayList<>();
        lines = new ArrayList<>();
        accessModifier = AccessModifier.PUBLIC;
        javadoc = null;
    }

    public Constructor setOwner(JavaBuilder owner) {
        this.owner = owner;
        return this;
    }

    public Constructor setAccessModifier(AccessModifier modifier) {
        accessModifier = modifier;
        return this;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public String getName() {
        if (owner == null) {
            return "unknown";
        }
        return owner.getName();
    }

    public String getType() {
        if (owner == null) {
            return "unknown";
        }
        return owner.getName();
    }

    public Constructor addParameter(String type, String name, String description) {
        return addParameter(new Param(type, name).setDescription(description));
    }

    public Constructor addParameter(Param parameter) {
        parameters.add(parameter);
        return this;
    }

    @Override
    public Constructor add(String code) {
        lines.add(code);
        return this;
    }

    @Override
    public Constructor append(Object object) {
        int index = lines.size() - 1;
        lines.set(index, lines.get(index) + object);
        return this;
    }

    public List<Param> getParameters() {
        return parameters;
    }

    public Iterable<String> getCodeLines() {
        return lines;
    }

    public Constructor setJavadoc(JavadocBuilder javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public String build(int indent) {
        StringBuilder builder = new StringBuilder();
        //JAVADOC
        builder.append(new JavadocBuilder(javadoc).append(this::parameterJavadocs).build(indent));
        //CONSTRUCTOR DECLARATION
        String access = accessModifier.get();
        if (owner.isSingleton()) {
            access = AccessModifier.PRIVATE.get();
        }
        indent(builder, indent).append(access).append(" ").append(getName()).append("(").append(buildParameters()).append(")").append(" ").append(BLOCK_START).append(LINE_END);
        //CODE
        for (String line : lines) {
            StringBuilder stringBuilder = new StringBuilder(line);
            Matcher matcher = PARAMETER_PATTERN.matcher(line);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String sub = stringBuilder.substring(start + 1, end);
                if (sub.endsWith(REPLACEMENT_END)) {
                    sub = sub.substring(0, sub.length() - REPLACEMENT_END.length());
                }
                if (sub.isEmpty()) {
                    stringBuilder.replace(start, end, REPLACEMENT_START);
                } else {
                    int index = Integer.parseInt(sub);
                    String methodName = parameters.get(index).getName();
                    stringBuilder.replace(start, end, methodName);
                }
            }
            indent(builder, indent + 1).append(stringBuilder).append(LINE_END);
        }
        indent(builder, indent).append(BLOCK_END).append(LINE_END);
        return builder.toString();
    }

    public JavadocBuilder parameterJavadocs(JavadocBuilder builder) {
        parameters.forEach(p -> builder.parameter(p.getName(), p.getDescription()));
        return builder;
    }

    private StringBuilder buildParameters() {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(p -> builder.append(capitalize(p.getType())).append(" ").append(p.getName().toLowerCase()).append(", "));
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2);
        }
        return builder;
    }
}
