package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 *
 * @author delma
 */
public class JavaBuilder implements CodeBlock {

    private final List<JavaBuilder> innerClasses;
    private final List<String> imports;
    private final List<Variable> variables;
    private final List<Method> methods;
    private final List<Constructor> constructors;
    private final List<String> lines;
    private String packageString;
    private JavadocBuilder javadoc;
    private AccessModifier accessModifier;
    private String name;
    private String superType;
    private ClassModifier classModifier;
    private final EnumSet<OptionalModifier> modifiers;

    public JavaBuilder() {
        packageString = null;
        imports = new ArrayList<>();
        innerClasses = new ArrayList<>();
        variables = new ArrayList<>();
        methods = new ArrayList<>();
        constructors = new ArrayList<>();
        lines = new ArrayList<>();
        modifiers = EnumSet.noneOf(OptionalModifier.class);
        javadoc = null;
        accessModifier = AccessModifier.PUBLIC;
        classModifier = ClassModifier.CLASS;
    }

    public JavaBuilder setPackage(String packageString) {
        this.packageString = packageString;
        return this;
    }

    public JavaBuilder addImport(String importString) {
        imports.add(importString);
        return this;
    }

    public JavaBuilder setAccess(AccessModifier modifier) {
        accessModifier = modifier;
        return this;
    }

    public JavaBuilder setName(String name) {
        this.name = capitalize(name);
        return this;
    }

    public JavaBuilder addInnerClass(UnaryOperator<JavaBuilder> operator) {
        innerClasses.add(operator.apply(new InnerJavaBuilder()));
        return this;
    }

    public JavaBuilder addMethod(Method method) {
        methods.add(method);
        method.setOwner(this);
        return this;
    }

    public JavaBuilder addVariable(Variable variable) {
        variables.add(variable);
        return this;
    }

    public JavaBuilder setJavadoc(JavadocBuilder builder) {
        this.javadoc = builder;
        return this;
    }

    public String build(Class<?> caller) {
        return build(caller, 0);
    }

    private String build(Class<?> caller, int indent) {
        StringBuilder builder = new StringBuilder();
        //PACKAGE
        if (packageString != null) {
            indent(builder, indent).append(PACKAGE).append(" ").append(packageString).append(STATEMENT_END).append(LINE_END);
            emptyLine(builder);
        }
        //IMPORTS
        if (!imports.isEmpty()) {
            imports.forEach(i -> indent(builder, indent).append(IMPORT).append(" ").append(i).append(STATEMENT_END).append(LINE_END));
            emptyLine(builder);
        }
        //JAVADOC
        builder.append(new JavadocBuilder(getGenerationWarning(this.getClass(), caller)).append(javadoc).build(indent));
        //CLASS DECLARATION
        String access = accessModifier.get();
        indent(builder, indent).append(access).append(" ");
        modifiers.forEach(m -> builder.append(m.get()).append(" "));
        builder.append(classModifier.get()).append(" ").append(name).append(" ");
        if (superType != null) {
            builder.append("extends ").append(superType).append(" ");
        }
        builder.append(BLOCK_START).append(LINE_END);
        {
            int classIndent = indent + 1;
            if (classModifier == ClassModifier.SINGLETON) {
                indent(builder, classIndent).append("INSTANCE").append(STATEMENT_END).append(LINE_END);
            }
            emptyLine(builder);
            //VARIABLES
            if (!variables.isEmpty()) {
                variables.forEach(v -> builder.append(v.build(classIndent)));
                emptyLine(builder);
            }
            //STATIC BLOCK
            if (!lines.isEmpty()) {
                indent(builder, classIndent).append("static").append(BLOCK_START).append(LINE_END);
                lines.forEach(l -> indent(builder, classIndent + 1).append(l).append(LINE_END));
                indent(builder, classIndent).append(BLOCK_END).append(LINE_END);
                emptyLine(builder);
            }
            //CONSTRUCTORS
            if (!constructors.isEmpty()) {
                constructors.forEach(c -> builder.append(c.build(classIndent)).append(LINE_END));
                emptyLine(builder);
            }
            //METHODS
            if (!methods.isEmpty()) {
                methods.forEach(m -> builder.append(m.build(classIndent)).append(LINE_END));
                emptyLine(builder);
            }
            //INNER CLASSES
            if (!innerClasses.isEmpty()) {
                innerClasses.forEach(c -> builder.append(c.build(caller, classIndent)).append(LINE_END));
            }
        }
        indent(builder, indent).append(BLOCK_END).append(LINE_END);
        return builder.toString();
    }

    public void addConstructor(Constructor constructor) {
        constructors.add(constructor);
        constructor.setOwner(this);
    }

    public String getName() {
        return name;
    }

    @Override
    public JavaBuilder add(String code) {
        lines.add(code);
        return this;
    }

    @Override
    public JavaBuilder append(Object code) {
        int index = lines.size() - 1;
        lines.set(index, lines.get(index) + code);
        return this;
    }

    public JavaBuilder setClassModifier(ClassModifier type) {
        classModifier = type;
        return this;
    }

    public JavaBuilder setExtends(String type) {
        superType = type;
        return this;
    }

    public ClassModifier getClassModifier() {
        return classModifier;
    }

    public JavaBuilder addModifier(OptionalModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    private class InnerJavaBuilder extends JavaBuilder {

        @Override
        public JavaBuilder setPackage(String packageString) {
            JavaBuilder.this.setPackage(packageString);
            return this;
        }

        @Override
        public JavaBuilder addImport(String importString) {
            JavaBuilder.this.addImport(importString);
            return this;
        }
    }
}
