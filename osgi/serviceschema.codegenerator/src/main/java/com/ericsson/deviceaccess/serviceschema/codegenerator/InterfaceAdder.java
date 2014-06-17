package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ActionDocument.Action;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Param;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Constant;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.JavaClass;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Javadoc;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Method;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Variable;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;
import com.ericsson.research.commonutil.StringUtil;

/**
 * This generates interfaces in
 * {@link com.ericsson.deviceaccess.api.service.*}.It does it by adding
 * necessary code to builder that it is given.
 *
 * @author delma
 */
public enum InterfaceAdder {

    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Adds necessary code to builder to generate interface for service.
     *
     * @param builder builder to add
     * @param version version of schema
     * @param service service which to add interface to
     */
    public static void addServiceInterface(JavaClass builder, Service service, String version) {
        builder.setPackage("com.ericsson.deviceaccess.api.service." + service.getCategory());

        builder.addImport("com.ericsson.deviceaccess.api.genericdevice.*");

        builder.setJavadoc(new Javadoc(StringUtil.setEndPunctuation(service.getDescription())));
        builder.setClassModifier(ClassModifier.INTERFACE);
        builder.setName(service.getName());
        builder.setExtends("GDService");

        addConstants(builder, version, service);
        addPropertyGetters(builder, service);
        addActionDefinitions(builder, service);
        addActionsResultTypes(builder, service);
    }

    /**
     * Adds needed constants to the builder.
     *
     * @param builder builder to add constants to
     * @param version version of schema
     * @param service service which constants are added
     */
    private static void addConstants(JavaClass builder, String version, Service service) {
        builder.addVariable(new Constant("String", "SCHEMA_VERSION", "\"" + version + "\""));

        builder.addVariable(new Constant("String", "SERVICE_NAME", "\"" + service.getName() + "\""));

        // Mandatory refresh properties action
        builder.addVariable(new Constant("String", "ACTION_refreshProperties", "\"refreshProperties\""));

        if (service.isSetActions()) {
            for (Action action : service.getActions().getActionArray()) {
                String name = action.getName();
                builder.addVariable(new Constant("String", "ACTION_" + name, "\"" + name + "\""));

                if (action.isSetArguments()) {
                    Parameter[] arguments = action.getArguments().getParameterArray();
                    addParameterConstants(builder, arguments, "ACTION_" + name + "_ARG");
                }

                if (action.isSetResults()) {
                    Parameter[] results = action.getResults().getParameterArray();
                    addParameterConstants(builder, results, "ACTION_" + name + "_RES");
                }
            }
        }

        // Mandatory last update time property
        builder.addVariable(new Constant("String", "PROP_lastUpdateTime", "\"lastUpdateTime\""));

        if (service.isSetProperties()) {
            Parameter[] properties = service.getProperties().getParameterArray();
            addParameterConstants(builder, properties, "PROP");
        }
    }

    /**
     * Adds constants for parameters.
     *
     * @param builder builder to add constants to
     * @param parameterArray parameters which constants are added
     * @param prefix prefix of constants
     */
    private static void addParameterConstants(JavaClass builder, Parameter[] parameterArray, String prefix) {
        for (Parameter parameter : parameterArray) {
            String name = parameter.getName();
            builder.addVariable(new Constant("String", prefix + "_" + name, "\"" + name + "\""));
            if (parameter.isSetValues()) {
                for (String value : parameter.getValues().getValueArray()) {
                    builder.addVariable(new Constant("String", "VALUE_" + prefix + "_" + name + "_" + value, "\"" + value + "\""));
                }
            }
        }
    }

    /**
     * Adds methods for getters of properties.
     *
     * @param builder builder to add methods to
     * @param service service which getters are added
     */
    private static void addPropertyGetters(JavaClass builder, Service service) {
        if (service.isSetProperties()) {
            for (Parameter property : service.getProperties().getParameterArray()) {
                String name = property.getName();
                Method method = new Method(StringUtil.getType(property.getType()), "get" + StringUtil.capitalize(name));
                method.setJavadoc(new Javadoc("Gets the property '").append(name).append("'.")
                        .line("Property description: ").append(StringUtil.setEndPunctuation(property.getDescription()))
                        .append(b -> getValidValuesJavadoc(b, property)));
                builder.addMethod(method);
            }
        }
    }

    /**
     * Adds Javadoc for parameter that tells what are allowed values for it.
     *
     * @param builder javadoc builder to add javadoc to
     * @param property property which Javadoc is added
     * @return builder
     */
    private static Javadoc getValidValuesJavadoc(Javadoc builder, Parameter property) {
        if ("String".equals(StringUtil.getType(property.getType()))) {
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

    /**
     * Adds list of allowed strings in html syntax to Javadoc.
     *
     * @param builder Javadoc builder to add Javadoc to
     * @param valueArray Allowed values for String
     * @return builder
     */
    private static Javadoc getValidValuesString(Javadoc builder, String[] valueArray) {
        builder.line("<ul>");
        for (String value : valueArray) {
            builder.line("<li>\"").append(value).append("\"</li>");
        }
        return builder.line("</ul>");
    }

    /**
     * Adds methods for actions of service to builder.
     *
     * @param builder builder to add methods to
     * @param service service which action methods are added
     */
    private static void addActionDefinitions(JavaClass builder, Service service) {
        if (service.isSetActions()) {
            for (Action action : service.getActions().getActionArray()) {
                String name = StringUtil.capitalize(action.getName());
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
                    for (Parameter parameter : action.getArguments().getParameterArray()) {
                        Param param = new Param(StringUtil.getType(parameter.getType()), parameter.getName());
                        param.setDescription(parameter.getDescription());
                        method.addParameter(param);
                    }
                }
                method.addThrow("GDException");
                builder.addMethod(method.setJavadoc(javadoc));
            }
        }
    }

    /**
     * Adds result types of service as inner classes to builder.
     *
     * @param builder builder to add inner classes to
     * @param service service which actions result POJOs are added
     */
    private static void addActionsResultTypes(JavaClass builder, Service service) {
        if (service.isSetActions()) {
            for (Action action : service.getActions().getActionArray()) {
                if (action.isSetResults()) {
                    String name = action.getName();
                    builder.addInnerClass(inner -> {
                        inner.setJavadoc(new Javadoc("Result from action ").append(name));
                        inner.setName(StringUtil.capitalize(name) + "Result");
                        inner.addModifier(OptionalModifier.FINAL);
                        inner.addModifier(OptionalModifier.STATIC);
                        for (Parameter result : action.getResults().getParameterArray()) {
                            Variable variable = new Variable(StringUtil.getType(result.getType()), result.getName());
                            variable.setAccessModifier(AccessModifier.PUBLIC);
                            variable.setJavadoc(new Javadoc(StringUtil.setEndPunctuation(result.getDescription())));
                            inner.addVariable(variable);
                        }
                    });
                }
            }
        }
    }
}
