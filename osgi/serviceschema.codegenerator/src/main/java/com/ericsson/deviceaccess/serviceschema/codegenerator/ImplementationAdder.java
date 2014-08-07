package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.common.util.StringUtil;
import com.ericsson.deviceaccess.service.xmlparser.ActionDocument.Action;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Param;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Constructor;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.JavaClass;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Javadoc;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Method;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;

/**
 * This generates classes in {@link com.ericsson.deviceaccess.spi.service.*}. It
 * does it by adding necessary code to builder that it is given.
 *
 * @author delma
 */
public enum ImplementationAdder {

    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Adds necessary code to builder to generate implementation class for
     * service.
     *
     * @param builder builder to add implementation to
     * @param service service which implementation to add
     * @param version version of schema
     */
    public static void addServiceImplementation(JavaClass builder, Service service, String version) {
        String name = service.getName();
        String category = service.getCategory();
        builder.setPackage("com.ericsson.deviceaccess.spi.service." + category);

        builder.addImport("com.ericsson.deviceaccess.spi.schema.based.*");
        builder.addImport("com.ericsson.deviceaccess.api.genericdevice.*");
        builder.addImport("com.ericsson.deviceaccess.spi.service.SchemaDefinitions");
        builder.addImport("com.ericsson.deviceaccess.spi.schema.ActionDefinition");
        builder.addImport("com.ericsson.deviceaccess.api.service." + category + "." + StringUtil.capitalize(name));

        builder.setJavadoc(new Javadoc(StringUtil.setEndPunctuation(service.getDescription())));
        builder.setName(StringUtil.capitalize(name) + "Base");
        builder.setExtends("SBServiceBase");
        builder.addImplements(StringUtil.capitalize(name));
        builder.setClassModifier(ClassModifier.ABSTRACT);

        addConstructor(builder, service);
        addSetActionsResultsOnContextMethods(builder, service);
        addPropertyGettersAndUpdaters(builder, service);

        // Mandatory refresh properties action
        Method method = new Method("void", "refreshProperties").setJavadoc(new Javadoc("Refresh all properties."));
        method.setAccessModifier(AccessModifier.PROTECTED).addModifier(OptionalModifier.ABSTRACT);
        builder.addMethod(method);
    }

    /**
     * Adds constructor of services implementation class
     *
     * @param builder builder to add constructor to
     * @param service service which constructor is added
     */
    private static void addConstructor(JavaClass builder, Service service) {
        Constructor constructor = new Constructor();
        builder.addConstructor(constructor);
        constructor.setJavadoc(new Javadoc("Creates the service and maps actions to methods that shall be defined by subclass."));

        constructor.add("super(SchemaDefinitions.INSTANCE.getServiceSchema(SERVICE_NAME));");

        // Mandatory refresh properties action
        constructor.addBlock("defineAction(ACTION_refreshProperties, context -> ", b -> {
            b.add("if(!context.isAuthorized()) return;");
            b.add("refreshProperties();");
        }).append(");");

        if (service.isSetActions()) {
            for (Action action : service.getActions().getActionArray()) {
                String name = action.getName();
                constructor.addBlock("defineAction(ACTION_" + name + ", context -> ", b -> {
                    b.add("if(!context.isAuthorized()) return;");
                    b.add("GDProperties arguments = context.getArguments();");
                    b.add(getResultDeclaration(action) + "execute" + StringUtil.capitalize(name) + "(");
                    if (action.isSetArguments()) {
                        boolean first = true;
                        for (Parameter argument : action.getArguments().getParameterArray()) {
                            if (!first) {
                                b.append(", ");
                            } else {
                                first = false;
                            }
                            b.add(JavaHelper.INDENT)
                                    .append("arguments.get")
                                    .append(StringUtil.capitalize(StringUtil.getType(argument.getType())))
                                    .append("Value(ACTION_").append(action.getName()).append("_ARG_").append(argument.getName()).append(")");
                        }
                    }
                    b.add(");");
                    if (action.isSetResults()) {
                        b.add("set" + StringUtil.capitalize(name) + "ResultOnContext(context, result);");
                    }
                }).append(");");
            }
        }
    }

    /**
     * Returns code to be added before method call to keep information returned
     * by the method (If there is any).
     *
     * @param action action which is called
     * @return code
     */
    private static String getResultDeclaration(Action action) {
        if (action.isSetResults()) {
            return StringUtil.capitalize(action.getName()) + "Result result = ";
        }
        return "";
    }

    /**
     * Adds setters for action results of service to builder. These are called
     * only by actions defined in constructor.
     *
     * @param builder builder to add methods to
     * @param service service which action result setter methods are added
     */
    private static void addSetActionsResultsOnContextMethods(JavaClass builder, Service service) {
        if (service.isSetActions()) {
            for (Action action : service.getActions().getActionArray()) {
                String name = action.getName();
                if (action.isSetResults()) {
                    Method method = new Method("void", "set" + StringUtil.capitalize(name) + "ResultOnContext");
                    method.setJavadoc(new Javadoc("Sets the result from the '").append(name).append("' action on the specified context."));
                    method.setAccessModifier(AccessModifier.PRIVATE);
                    method.addParameter(new Param("GDActionContext", "context").setDescription("action context to set result in"));
                    method.addParameter(new Param(StringUtil.capitalize(name) + "Result", "result").setDescription("result to set"));

                    method.add("GDProperties value = #0.getResult().getValue();");
                    for (Parameter result : action.getResults().getParameterArray()) {
                        String type = StringUtil.capitalize(StringUtil.getType(result.getType()));
                        method.add("value.set" + type + "Value(ACTION_" + name + "_RES_" + result.getName() + ", #1." + result.getName() + ");");
                    }
                    builder.addMethod(method);
                }
            }
        }
    }

    /**
     * Adds getters and updaters for properties of service to builder. Getters
     * can be called by anyone and updaters are called by extender of
     * implementation class.
     *
     * @param builder builder to add methods to
     * @param service service which properties getters and setters methods are
     * added
     */
    private static void addPropertyGettersAndUpdaters(JavaClass builder, Service service) {
        if (service.isSetProperties()) {
            for (Parameter property : service.getProperties().getParameterArray()) {
                String name = property.getName();
                String type = property.getType();

                Method method = new Method(StringUtil.getType(type), "get" + StringUtil.capitalize(name));
                method.addModifier(OptionalModifier.FINAL);
                method.setJavadoc(new Javadoc().inherit());
                method.add("return getProperties().get" + StringUtil.capitalize(StringUtil.getType(type)) + "Value(PROP_" + name + ");");
                builder.addMethod(method);

                method = new Method("void", "update" + StringUtil.capitalize(name));
                method.setAccessModifier(AccessModifier.PROTECTED).addModifier(OptionalModifier.FINAL);
                method.setJavadoc(new Javadoc("Update the '").append(name).append("' property. To be used by concrete implementations of the service."));
                method.addParameter(new Param(StringUtil.getType(property.getType()), "value").setDescription("value to be set"));
                method.add("getProperties().set" + StringUtil.capitalize(StringUtil.getType(type)) + "Value(PROP_" + name + ", #0);");
                builder.addMethod(method);
            }
        }
    }
}
