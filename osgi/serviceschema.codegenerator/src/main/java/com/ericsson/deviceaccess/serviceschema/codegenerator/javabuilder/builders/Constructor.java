package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders;

import com.ericsson.commonutil.StringUtil;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.AbstractCodeBlock;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Callable;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlock;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_PATTERN;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.REPLACEMENT_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Param;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Builder of Constructor for {@link JavaClass}.
 *
 * @author delma
 */
public class Constructor extends AbstractCodeBlock implements Callable {

    private AccessModifier accessModifier;
    private final List<Param> parameters;
    private Javadoc javadoc;
    private JavaClass owner;

    /**
     * Creates constructor.
     */
    public Constructor() {
        parameters = new ArrayList<>();
        accessModifier = AccessModifier.PUBLIC;
        javadoc = null;
    }

    /**
     * To be called {@link JavaClass} to set it as owner of this.
     *
     * @param owner
     * @return this
     */
    protected Constructor setOwner(JavaClass owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public Constructor setAccessModifier(AccessModifier modifier) {
        accessModifier = modifier;
        return this;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public String getName() {
        if (owner == null) {
            return "unknown";
        }
        return owner.getName();
    }

    @Override
    public String getType() {
        if (owner == null) {
            return "unknown";
        }
        return owner.getName();
    }

    @Override
    public Constructor addParameter(Param parameter) {
        parameters.add(parameter);
        return this;
    }

    @Override
    public Constructor setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    /**
     * Builds Constructor to string with specified indent
     *
     * @param indent how much indent there is
     * @return builded string
     */
    public String build(int indent) {
        StringBuilder builder = new StringBuilder();
        addJavadoc(builder, indent);
        addConstructorDeclaration(builder, indent);
        {
            addCode(builder, indent + 1);
        }
        addConstructorEnd(builder, indent);
        return builder.toString();
    }

    /**
     * Adds end of constructor to builder
     *
     * @param builder builder to add constructors ending
     * @param indent indent
     */
    private void addConstructorEnd(StringBuilder builder, int indent) {
        indent(builder, indent).append(BLOCK_END).append(LINE_END);
    }

    /**
     * Adds code that is in constructor
     *
     * @param builder builder to add code
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
     * Adds declaration of constructor
     *
     * @param builder builder to add constructors declaration
     * @param indent indent
     */
    private void addConstructorDeclaration(StringBuilder builder, int indent) {
        AccessModifier access = accessModifier;
        ClassModifier classModifier = owner.getClassModifier();
        if (classModifier == ClassModifier.SINGLETON || classModifier == ClassModifier.ENUM) {
            access = AccessModifier.PRIVATE;
        }
        indent(builder, indent).append(access.get()).append(" ").append(getName()).append("(").append(buildParameters()).append(")").append(" ").append(BLOCK_START).append(LINE_END);
    }

    /**
     * Adds Javadoc of constructor
     *
     * @param builder builder to add javadoc to
     * @param indent indent
     */
    private void addJavadoc(StringBuilder builder, int indent) {
        builder.append(new Javadoc(javadoc).append(this::parameterJavadocs).build(indent));
    }

    /**
     * Adds Javadoc of parameters
     *
     * @param builder javadoc builder to add parameters javadoc to
     * @return builder
     */
    private Javadoc parameterJavadocs(Javadoc builder) {
        parameters.forEach(p -> builder.parameter(p.getName(), p.getDescription()));
        return builder;
    }

    /**
     * Builds parameters for constructors declaration
     *
     * @return parameters
     */
    private StringBuilder buildParameters() {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(p -> builder.append(StringUtil.capitalize(p.getType())).append(" ").append(p.getName().toLowerCase()).append(", "));
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
    public Constructor addBlock(String code, Consumer<CodeBlock> block) {
        super.addBlock(code, block);
        return this;
    }
}
