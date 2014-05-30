package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ActionDocument;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Constant;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaClass;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Javadoc;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Method;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Param;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Variable;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;

/**
 *
 * @author delma
 */
public enum InterfaceAdder {

    INSTANCE;

    public static void addServiceInterface(JavaClass builder, String version, ServiceDocument.Service service) {
        builder.setPackage("com.ericsson.deviceaccess.api.service." + service.getCategory());

        builder.addImport("com.ericsson.deviceaccess.api.GenericDeviceService");
        builder.addImport("com.ericsson.deviceaccess.api.GenericDeviceException");

        builder.setJavadoc(new Javadoc(StringHelper.setEndPunctuation(service.getDescription())));
        builder.setClassModifier(ClassModifier.INTERFACE);
        builder.setName(service.getName());
        builder.setExtends("GenericDeviceService");

        builder.addVariable(new Constant("String", "SCHEMA_VERSION", "\"" + version + "\""));
        addConstants(builder, service);
        addPropertyGetters(builder, service);
        addActionDefinitions(builder, service);
        addActionsResultTypes(builder, service);
    }

    private static void addConstants(JavaClass builder, ServiceDocument.Service service) {
        builder.addVariable(new Constant("String", "SERVICE_NAME", "\"" + service.getName() + "\""));

        // Mandatory refresh properties action
        builder.addVariable(new Constant("String", "ACTION_refreshProperties", "\"refreshProperties\""));

        if (service.isSetActions()) {
            for (ActionDocument.Action action : service.getActions().getActionArray()) {
                String name = action.getName();
                builder.addVariable(new Constant("String", "ACTION_" + name, "\"" + name + "\""));

                if (action.isSetArguments()) {
                    ParameterDocument.Parameter[] arguments = action.getArguments().getParameterArray();
                    addParameterConstants(builder, arguments, "ACTION_" + name + "_ARG");
                }

                if (action.isSetResults()) {
                    ParameterDocument.Parameter[] results = action.getResults().getParameterArray();
                    addParameterConstants(builder, results, "ACTION_" + name + "_RES");
                }
            }
        }

        // Mandatory last update time property
        builder.addVariable(new Constant("String", "PROP_lastUpdateTime", "\"lastUpdateTime\""));

        if (service.isSetProperties()) {
            ParameterDocument.Parameter[] properties = service.getProperties().getParameterArray();
            addParameterConstants(builder, properties, "PROP");
        }
    }

    private static void addParameterConstants(JavaClass builder, ParameterDocument.Parameter[] parameterArray, String prefix) {
        for (ParameterDocument.Parameter parameter : parameterArray) {
            String name = parameter.getName();
            builder.addVariable(new Constant("String", prefix + "_" + name, "\"" + name + "\""));
            if (parameter.isSetValues()) {
                for (String value : parameter.getValues().getValueArray()) {
                    builder.addVariable(new Constant("String", "VALUE_" + prefix + "_" + name + "_" + value, "\"" + value + "\""));
                }
            }
        }
    }

    private static void addPropertyGetters(JavaClass builder, ServiceDocument.Service service) {
        if (service.isSetProperties()) {
            for (ParameterDocument.Parameter property : service.getProperties().getParameterArray()) {
                String name = property.getName();
                Method method = new Method(StringHelper.getType(property.getType()), "get" + StringHelper.capitalize(name));
                method.setJavadoc(new Javadoc("Gets the property '").append(name).append("'.")
                        .line("Property description: ").append(StringHelper.setEndPunctuation(property.getDescription()))
                        .append(b -> getValidValuesJavadoc(b, property)));
                builder.addMethod(method);
            }
        }
    }

    private static Javadoc getValidValuesJavadoc(Javadoc builder, ParameterDocument.Parameter property) {
        if ("String".equals(StringHelper.getType(property.getType()))) {
            if (property.isSetValues()) {
                builder.line("Valid values:");
                getValidValuesString(builder, property.getValues().getValueArray());
            }
            return builder;
        }
        if (property.getMin() != null) {
            builder.line("Min: ").append(property.getMin());
        }
        if (property.getMax() != null) {
            builder.line("Max: ").append(property.getMax());
        }
        return builder;
    }

    private static Javadoc getValidValuesString(Javadoc builder, String[] valueArray) {
        builder.line("<ul>");
        for (String value : valueArray) {
            builder.line("<li>\"").append(value).append("\"</li>");
        }
        return builder.line("</ul>");
    }

    private static void addActionDefinitions(JavaClass builder, ServiceDocument.Service service) {
        if (service.isSetActions()) {
            for (ActionDocument.Action action : service.getActions().getActionArray()) {
                String name = StringHelper.capitalize(action.getName());
                Javadoc javadoc = new Javadoc();

                String result = "void";
                if (action.isSetResults()) {
                    result = name + "Result";
                    javadoc.result("{@link " + result + "}");
                }
                javadoc.line("Execute the action '").append(action.getName()).append("'.");
                javadoc.line("Action description: ").append(action.getDescription());
                Method method = new Method(result, "execute" + name);
                if (action.isSetArguments()) {
                    for (ParameterDocument.Parameter parameter : action.getArguments().getParameterArray()) {
                        Param param = new Param(StringHelper.getType(parameter.getType()), parameter.getName());
                        param.setDescription(parameter.getDescription());
                        method.addParameter(param);
                    }
                }
                method.addThrow("GenericDeviceException");
                builder.addMethod(method.setJavadoc(javadoc));
            }
        }
    }

    private static void addActionsResultTypes(JavaClass builder, ServiceDocument.Service service) {
        if (service.isSetActions()) {
            for (ActionDocument.Action action : service.getActions().getActionArray()) {
                if (action.isSetResults()) {
                    String name = action.getName();
                    builder.addInnerClass(inner -> {
                        inner.setJavadoc(new Javadoc("Result from action ").append(name));
                        inner.setName(StringHelper.capitalize(name) + "Result");
                        inner.addModifier(OptionalModifier.FINAL);
                        inner.addModifier(OptionalModifier.STATIC);
                        for (ParameterDocument.Parameter result : action.getResults().getParameterArray()) {
                            Variable variable = new Variable(StringHelper.getType(result.getType()), result.getName());
                            variable.setAccessModifier(AccessModifier.PUBLIC);
                            variable.setJavadoc(new Javadoc(StringHelper.setEndPunctuation(result.getDescription())));
                            inner.addVariable(variable);
                        }
                        return inner;
                    });
                }
            }
        }
    }
}