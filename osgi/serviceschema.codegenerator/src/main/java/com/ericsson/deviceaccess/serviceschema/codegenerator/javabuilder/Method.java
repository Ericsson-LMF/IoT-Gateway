package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.PARAMETER_PATTERN;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.STATEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 *
 * @author delma
 */
public class Method extends AbstractCodeBlock {

    private AccessModifier accessModifier;
    private final String name;
    private final String type;
    private final List<Param> parameters;
    private final List<String> throwList;
    private final EnumSet<OptionalModifier> modifiers;
    private Javadoc javadoc;
    private JavaClass owner;

    public Method(String type, String name) {
        this.type = type;
        this.name = name;
        parameters = new ArrayList<>();
        throwList = new ArrayList<>();
        modifiers = EnumSet.noneOf(OptionalModifier.class);
        accessModifier = AccessModifier.PUBLIC;
        javadoc = null;
    }

    public Method setOwner(JavaClass owner) {
        this.owner = owner;
        return this;
    }

    public Method setAccessModifier(AccessModifier modifier) {
        accessModifier = modifier;
        return this;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Method addParameter(String type, String name, String description) {
        return addParameter(new Param(type, name).setDescription(description));
    }

    public Method addParameter(Param parameter) {
        parameters.add(parameter);
        return this;
    }

    public List<Param> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public Iterable<String> getCodeLines() {
        return lines;
    }

    public Method setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public String build(int indent) {
        StringBuilder builder = new StringBuilder();
        //JAVADOC
        builder.append(new Javadoc(javadoc).append(this::parameterJavadocs).build(indent));
        //METHOD DECLARATION
        String access = accessModifier.get();
        indent(builder, indent).append(access).append(" ");
        modifiers.forEach(m -> builder.append(m.get()).append(" "));
        builder.append(type).append(" ").append(name).append("(").append(buildParameters()).append(")");
        if (!throwList.isEmpty()) {
            builder.append(" throws ");
            throwList.forEach(t -> builder.append(t).append(", "));
            builder.setLength(builder.length() - 2);
        }
        boolean isAbstract = modifiers.contains(OptionalModifier.ABSTRACT) || (owner != null && owner.getClassModifier() == ClassModifier.INTERFACE);
        if (isAbstract) {
            builder.append(STATEMENT_END).append(LINE_END);
            return builder.toString();
        }
        builder.append(" ").append(BLOCK_START).append(LINE_END);
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

    private Javadoc parameterJavadocs(Javadoc builder) {
        parameters.forEach(p -> builder.parameter(p.getName(), p.getDescription()));
        return builder;
    }

    private StringBuilder buildParameters() {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(p -> builder.append(p.getType()).append(" ").append(p.getName().toLowerCase()).append(", "));
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2);
        }
        return builder;
    }

    public Method addThrow(String exception) {
        throwList.add(exception);
        return this;
    }

    @Override
    public Method add(String code) {
        super.add(code);
        return this;
    }

    @Override
    public Method append(Object code) {
        super.append(code);
        return this;
    }

    @Override
    public Method addBlock(Object object, Consumer<CodeBlock> block) {
        super.addBlock(object, block);
        return this;
    }

    public Method addModifier(OptionalModifier modifier) {
        modifiers.add(modifier);
        return this;
    }
}
