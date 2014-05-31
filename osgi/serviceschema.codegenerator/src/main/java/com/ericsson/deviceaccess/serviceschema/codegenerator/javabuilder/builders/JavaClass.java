package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders;

import com.ericsson.deviceaccess.serviceschema.codegenerator.StringHelper;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlock;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlockImpl;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Component;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Modifierable;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.BLOCK_START;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.EXTENDS;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.IMPLEMENTS;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.IMPORT;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.LINE_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.PACKAGE;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.STATEMENT_END;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.emptyLine;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.getGenerationWarning;
import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.indent;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for Java Class
 *
 * @author delma
 */
public class JavaClass extends CodeBlockImpl implements Component, Modifierable {

    private final List<JavaClass> innerClasses;
    private final List<String> imports;
    private final List<Variable> variables;
    private final List<Method> methods;
    private final List<Constructor> constructors;
    private final List<String> interfaces;
    private String packageString;
    private Javadoc javadoc;
    private AccessModifier accessModifier;
    private String name;
    private String superType;
    private ClassModifier classModifier;
    private final EnumSet<OptionalModifier> modifiers;

    /**
     * Creates new builder for Java Class
     */
    public JavaClass() {
        packageString = null;
        imports = new ArrayList<>();
        innerClasses = new ArrayList<>();
        variables = new ArrayList<>();
        methods = new ArrayList<>();
        constructors = new ArrayList<>();
        interfaces = new ArrayList<>();
        modifiers = EnumSet.noneOf(OptionalModifier.class);
        javadoc = null;
        accessModifier = AccessModifier.PUBLIC;
        classModifier = ClassModifier.CLASS;
    }

    /**
     * Sets the package this class is in
     * @param packageString package
     * @return this
     */
    public JavaClass setPackage(String packageString) {
        this.packageString = packageString;
        return this;
    }

    /**
     * Adds import for this class
     * @param importString import
     * @return this
     */
    public JavaClass addImport(String importString) {
        imports.add(importString);
        return this;
    }

    @Override
    public JavaClass setAccessModifier(AccessModifier modifier) {
        accessModifier = modifier;
        return this;
    }

    /**
     * Sets name of this class
     * @param name name of this class
     * @return this
     */
    public JavaClass setName(String name) {
        this.name = StringHelper.capitalize(name);
        return this;
    }

    /**
     * Adds inner class to this class
     * @param consumer Consumer with to edit inner clas
     * @return this
     */
    public JavaClass addInnerClass(Consumer<JavaClass> consumer) {
        JavaClass inner = new InnerJavaBuilder();
        consumer.accept(inner);
        innerClasses.add(inner);
        return this;
    }

    /**
     * Adds method to this class
     * @param method method to be added
     * @return this
     */
    public JavaClass addMethod(Method method) {
        methods.add(method);
        method.setOwner(this);
        return this;
    }

    /**
     * Adds class variable to this class
     * @param variable class variable
     * @return this
     */
    public JavaClass addVariable(Variable variable) {
        variables.add(variable);
        return this;
    }

    @Override
    public JavaClass setJavadoc(Javadoc builder) {
        this.javadoc = builder;
        return this;
    }

    /**
     * Builds Constructor to string
     *
     * @return builded string
     */
    public String build() {
        return build(0);
    }

    private String build(int indent) {
        StringBuilder builder = new StringBuilder();
        addPackage(builder, indent);
        addImports(builder, indent);
        addJavadoc(builder, indent);
        addClassDeclaration(builder, indent);
        {
            addClassContent(builder, indent + 1);
        }
        addClassEnd(builder, indent);
        return builder.toString();
    }

    private void addClassContent(StringBuilder builder, int indent) {
        addEnums(builder, indent);
        addVariables(builder, indent);
        addStaticBlock(builder, indent);
        addConstructors(builder, indent);
        addMethods(builder, indent);
        addInnerClasses(builder, indent);
    }

    private void addEnums(StringBuilder builder, int classIndent) {
        if (classModifier == ClassModifier.SINGLETON) {
            indent(builder, classIndent).append("INSTANCE").append(STATEMENT_END).append(LINE_END);
            emptyLine(builder);
        }
    }

    private void addInnerClasses(StringBuilder builder, int classIndent) {
        if (!innerClasses.isEmpty()) {
            innerClasses.forEach(c -> builder.append(c.build(classIndent)).append(LINE_END));
            emptyLine(builder);
        }
    }

    private void addMethods(StringBuilder builder, int classIndent) {
        if (!methods.isEmpty()) {
            methods.forEach(m -> builder.append(m.build(classIndent)).append(LINE_END));
            emptyLine(builder);
        }
    }

    private void addConstructors(StringBuilder builder, int classIndent) {
        if (!constructors.isEmpty()) {
            constructors.forEach(c -> builder.append(c.build(classIndent)).append(LINE_END));
            emptyLine(builder);
        }
    }

    private void addStaticBlock(StringBuilder builder, int classIndent) {
        if (!lines.isEmpty()) {
            indent(builder, classIndent).append("static").append(BLOCK_START).append(LINE_END);
            lines.forEach(l -> indent(builder, classIndent + 1).append(l).append(LINE_END));
            indent(builder, classIndent).append(BLOCK_END).append(LINE_END);
            emptyLine(builder);
        }
    }

    private void addVariables(StringBuilder builder, int classIndent) {
        if (!variables.isEmpty()) {
            variables.forEach(v -> builder.append(v.build(classIndent)));
            emptyLine(builder);
        }
    }

    private void addClassDeclaration(StringBuilder builder, int indent) {
        String access = accessModifier.get();
        indent(builder, indent).append(access).append(" ");
        modifiers.forEach(m -> builder.append(m.get()).append(" "));
        builder.append(classModifier.get()).append(" ").append(name).append(" ");
        addSuperType(builder);
        addImplements(builder);
        builder.append(BLOCK_START).append(LINE_END);
        emptyLine(builder);
    }

    private void addImplements(StringBuilder builder) {
        if (!interfaces.isEmpty()) {
            builder.append(IMPLEMENTS).append(" ");
            interfaces.forEach(i -> builder.append(i).append(", "));
            builder.setLength(builder.length() - 2);
            builder.append(" ");
        }
    }

    private void addSuperType(StringBuilder builder) {
        if (superType != null) {
            builder.append(EXTENDS).append(" ").append(superType).append(" ");
        }
    }

    private void addClassEnd(StringBuilder builder, int indent) {
        indent(builder, indent).append(BLOCK_END).append(LINE_END);
    }

    private void addJavadoc(StringBuilder builder, int indent) {
        String warning = getGenerationWarning(this.getClass());
        builder.append(new Javadoc(warning).append(javadoc).build(indent));
    }

    private void addImports(StringBuilder builder, int indent) {
        if (!imports.isEmpty()) {
            imports.forEach(i -> indent(builder, indent).append(IMPORT).append(" ").append(i).append(STATEMENT_END).append(LINE_END));
            emptyLine(builder);
        }
    }

    private void addPackage(StringBuilder builder, int indent) {
        if (packageString != null) {
            indent(builder, indent).append(PACKAGE).append(" ").append(packageString).append(STATEMENT_END).append(LINE_END);
            emptyLine(builder);
        }
    }

    /**
     * Adds constructor to this class
     * @param constructor constructor to be added
     * @return this
     */
    public JavaClass addConstructor(Constructor constructor) {
        constructors.add(constructor);
        constructor.setOwner(this);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets what type of class this is
     * @param type type of class
     * @return this
     */
    public JavaClass setClassModifier(ClassModifier type) {
        classModifier = type;
        return this;
    }

    /**
     * Sets what this class extends
     * @param type super class
     * @return this
     */
    public JavaClass setExtends(String type) {
        superType = type;
        return this;
    }

    /**
     * Gets the type of the class
     * @return this
     */
    public ClassModifier getClassModifier() {
        return classModifier;
    }

    @Override
    public JavaClass addModifier(OptionalModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    /**
     * Adds interface that this class implements
     * @param interfaceString interface to implement
     * @return this
     */
    public JavaClass addImplements(String interfaceString) {
        interfaces.add(interfaceString);
        return this;
    }

    @Override
    public JavaClass add(String code) {
        super.add(code);
        return this;
    }

    @Override
    public JavaClass append(Object code) {
        super.append(code);
        return this;
    }

    @Override
    public JavaClass addBlock(String code, Consumer<CodeBlock> block) {
        super.addBlock(code, block);
        return this;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public String getType() {
        return getName();
    }

    private class InnerJavaBuilder extends JavaClass {

        @Override
        public JavaClass setPackage(String packageString) {
            JavaClass.this.setPackage(packageString);
            return this;
        }

        @Override
        public JavaClass addImport(String importString) {
            JavaClass.this.addImport(importString);
            return this;
        }
    }
}
