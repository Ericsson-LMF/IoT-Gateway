package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.AbstractCodeBlock;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Callable;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlock;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_PATTERN;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.STATEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Modifierable;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Param;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier.INTERFACE;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Builder of Method for {@link JavaClass}
 *
 * @author delma
 */
public class Method extends AbstractCodeBlock implements Callable, Modifierable {

    private AccessModifier accessModifier;
    private final String name;
    private final String type;
    private final List<Param> parameters;
    private final List<String> throwList;
    private final EnumSet<OptionalModifier> modifiers;
    private Javadoc javadoc;
    private JavaClass owner;

    /**
     * Creates new Method with type and name
     *
     * @param type
     * @param name
     */
    public Method(String type, String name) {
        this.type = type;
        this.name = name;
        parameters = new ArrayList<>();
        throwList = new ArrayList<>();
        modifiers = EnumSet.noneOf(OptionalModifier.class);
        accessModifier = AccessModifier.PUBLIC;
        javadoc = null;
    }

    /**
     * To be called {@link JavaClass} to set it as owner of this
     *
     * @param owner
     * @return this
     */
    protected Method setOwner(JavaClass owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public Method setAccessModifier(AccessModifier modifier) {
        accessModifier = modifier;
        return this;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Method addParameter(Param parameter) {
        parameters.add(parameter);
        return this;
    }

    @Override
    public Method setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    /**
     * Builds Method to string with specified indent
     *
     * @param clazz
     * @param indent how much indent there is
     * @return builded string
     */
    public String build(JavaClass clazz, int indent) {
        StringBuilder builder = new StringBuilder();
        addJavadoc(builder, indent);
        if (addMethodDeclaration(clazz, builder, indent)) {
            return builder.toString();
        }
        {
            addCode(builder, indent + 1);
        }
        addMethodEnd(builder, indent);
        return builder.toString();
    }

    /**
     * Adds code to method
     *
     * @param builder builder to add code to
     * @param indent indent
     * @throws NumberFormatException
     */
    private void addCode(StringBuilder builder, int indent) throws NumberFormatException {
        for (String line : lines) {
            StringBuilder stringBuilder = new StringBuilder(line);
            Matcher matcher = REPLACEMENT_PATTERN.matcher(line);
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
            indent(builder, indent).append(stringBuilder).append(LINE_END);
        }
    }

    /**
     * Adds methods end to builder
     *
     * @param builder builder to add methods end
     * @param indent indent
     */
    private void addMethodEnd(StringBuilder builder, int indent) {
        indent(builder, indent).append(BLOCK_END).append(LINE_END);
    }

    /**
     * Adds methods declaration to builder
     *
     * @param builder builder to add declaration to
     * @param indent indent
     * @return is there code or not
     */
    private boolean addMethodDeclaration(JavaClass clazz, StringBuilder builder, int indent) {
        indent(builder, indent);
        if (clazz.getClassModifier() != INTERFACE) {
            builder.append(accessModifier.get()).append(" ");
        }
        modifiers.forEach(m -> builder.append(m.get()).append(" "));
        builder.append(type).append(" ").append(name).append("(").append(buildParameters()).append(")");
        addThrows(builder);
        boolean isAbstract = modifiers.contains(OptionalModifier.ABSTRACT) || owner != null && owner.getClassModifier() == INTERFACE;
        if (isAbstract) {
            builder.append(STATEMENT_END).append(LINE_END);
            return true;
        }
        builder.append(" ").append(BLOCK_START).append(LINE_END);
        return false;
    }

    /**
     * Adds thrown exceptions to declaration
     *
     * @param builder builder to add throw clauses to
     */
    private void addThrows(StringBuilder builder) {
        if (!throwList.isEmpty()) {
            builder.append(" throws ");
            throwList.forEach(t -> builder.append(t).append(", "));
            builder.setLength(builder.length() - 2);
        }
    }

    /**
     * Adds Javadoc for method
     *
     * @param builder builder to add Javadoc to
     * @param indent indent
     */
    private void addJavadoc(StringBuilder builder, int indent) {
        builder.append(new Javadoc(javadoc)
                .append(this::parameterJavadocs)
                .append(this::throwJavadocs)
                .build(indent));
    }

    /**
     * Adds parameters Javadoc to builder
     *
     * @param builder Javadoc builder to add Javadoc to
     * @return builder
     */
    private Javadoc parameterJavadocs(Javadoc builder) {
        parameters.forEach(p -> builder.parameter(p.getName(), p.getDescription()));
        return builder;
    }

    /**
     * Adds throws Javadoc to builder
     *
     * @param builder Javadoc builder to add Javadoc to
     * @return builder
     */
    private Javadoc throwJavadocs(Javadoc builder) {
        throwList.forEach(t -> builder.exception(t, ""));
        return builder;
    }

    /**
     * Builds parameters for method declaration
     *
     * @return parameters
     */
    private StringBuilder buildParameters() {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(p -> builder.append(p.getType()).append(" ").append(p.getName()).append(", "));
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2);
        }
        return builder;
    }

    /**
     * Adds throws clause
     *
     * @param exception exception thrown
     * @return this
     */
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
    public Method addBlock(String code, Consumer<CodeBlock> block) {
        super.addBlock(code, block);
        return this;
    }

    @Override
    public Method addModifier(OptionalModifier modifier) {
        modifiers.add(modifier);
        return this;
    }
}
