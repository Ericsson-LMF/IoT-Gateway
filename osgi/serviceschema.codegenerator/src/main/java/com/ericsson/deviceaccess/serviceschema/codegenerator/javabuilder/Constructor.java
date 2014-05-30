package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import com.ericsson.deviceaccess.serviceschema.codegenerator.StringHelper;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.PARAMETER_PATTERN;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 *
 * @author delma
 */
public class Constructor extends AbstractCodeBlock {

    private AccessModifier accessModifier;
    private final List<Param> parameters;
    private Javadoc javadoc;
    private JavaClass owner;

    public Constructor() {
        parameters = new ArrayList<>();
        accessModifier = AccessModifier.PUBLIC;
        javadoc = null;
    }

    public Constructor setOwner(JavaClass owner) {
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

    public Constructor setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public String build(int indent) {
        StringBuilder builder = new StringBuilder();
        //JAVADOC
        builder.append(new Javadoc(javadoc).append(this::parameterJavadocs).build(indent));
        //CONSTRUCTOR DECLARATION
        String access = accessModifier.get();
        if (owner.getClassModifier() == ClassModifier.SINGLETON || owner.getClassModifier() == ClassModifier.ENUM) {
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

    public Javadoc parameterJavadocs(Javadoc builder) {
        parameters.forEach(p -> builder.parameter(p.getName(), p.getDescription()));
        return builder;
    }

    private StringBuilder buildParameters() {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(p -> builder.append(StringHelper.capitalize(p.getType())).append(" ").append(p.getName().toLowerCase()).append(", "));
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2);
        }
        return builder;
    }

    @Override
    public Constructor add(String code) {
        super.add(code);
        return this;
    }

    @Override
    public Constructor append(Object code) {
        super.append(code);
        return this;
    }

    @Override
    public Constructor addBlock(Object object, Consumer<CodeBlock> block) {
        super.addBlock(object, block);
        return this;
    }
}
