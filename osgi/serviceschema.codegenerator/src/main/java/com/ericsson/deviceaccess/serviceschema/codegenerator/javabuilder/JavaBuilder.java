package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 *
 * @author delma
 */
public class JavaBuilder implements CodeBlock{

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
    private boolean singleton;

    public JavaBuilder() {
        packageString = null;
        imports = new ArrayList<>();
        innerClasses = new ArrayList<>();
        variables = new ArrayList<>();
        methods = new ArrayList<>();
        constructors = new ArrayList<>();
        lines = new ArrayList<>();
        javadoc = null;
        accessModifier = AccessModifier.PUBLIC;
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
        return build(caller, new StringBuilder(), 0);
    }

    private String build(Class<?> caller, StringBuilder builder, int indent) {
        //PACKAGE
        indent(builder, indent).append(PACKAGE).append(" ").append(packageString).append(STATEMENT_END).append(LINE_END);
        emptyLine(builder);
        //IMPORTS
        imports.forEach(i -> indent(builder, indent).append(IMPORT).append(" ").append(i).append(STATEMENT_END).append(LINE_END));
        emptyLine(builder);
        //JAVADOC
        builder.append(new JavadocBuilder(getGenerationWarning(this.getClass(), caller)).append(javadoc).build(indent));
        //CLASS DECLARATION
        String access = accessModifier.get();
        String type = singleton ? "enum" : "class";
        indent(builder, indent).append(access).append(" ").append(type).append(" ").append(name).append(BLOCK_START).append(LINE_END);
        {
            int classIndent = indent + 1;
            if (singleton) {
                indent(builder, classIndent).append("INSTANCE").append(STATEMENT_END).append(LINE_END);
            }
            emptyLine(builder);
            //VARIABLES
            variables.forEach(v -> builder.append(v.build(classIndent)));
            emptyLine(builder);
            //STATIC BLOCK
            if(!lines.isEmpty()){
                indent(builder, classIndent).append("static").append(BLOCK_START).append(LINE_END);
                lines.forEach(l -> indent(builder, classIndent + 1).append(l).append(LINE_END));
                indent(builder, classIndent).append(BLOCK_END).append(LINE_END);
                emptyLine(builder);
            }  
            //CONSTRUCTORS
            constructors.forEach(c -> builder.append(c.build(classIndent)));
            emptyLine(builder);
            //METHODS
            methods.forEach(m -> builder.append(m.build(classIndent)));
            emptyLine(builder);
            //INNER CLASSES
            innerClasses.forEach(c -> builder.append(c.build(caller, builder, classIndent)));
            indent(builder, indent);
        }
        builder.append(BLOCK_END).append(LINE_END);
        return builder.toString();
    }

    /**
     * Sets if this class is singleton or not
     * 
     * @param singleton 
     */
    public void setSigleton(boolean singleton) {
        this.singleton = singleton;

    }
    
    public boolean isSingleton() {
        return singleton;
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
