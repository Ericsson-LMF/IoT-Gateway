package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ActionDocument.Action;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Constructor;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.JavaClass;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Javadoc;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Method;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Param;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;

/**
 *
 * @author delma
 */
public enum ImplementationAdder {

    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Adds implementation to builder.
     * @param builder builder to add
     * @param service service which implementation to add
     */
    public static void addServiceImplementation(JavaClass builder, Service service) {
        String name = service.getName();
        String category = service.getCategory();
        builder.setPackage("com.ericsson.deviceaccess.spi.service." + category);

        builder.addImport("com.ericsson.deviceaccess.spi.schema.SchemaBasedServiceBase");
        builder.addImport("com.ericsson.deviceaccess.spi.service.SchemaDefinitions");
        builder.addImport("com.ericsson.deviceaccess.api.GenericDeviceProperties");
        builder.addImport("com.ericsson.deviceaccess.api.GenericDeviceActionContext");
        builder.addImport("com.ericsson.deviceaccess.api.GenericDeviceException");
        builder.addImport("com.ericsson.deviceaccess.spi.schema.ActionDefinition");
        builder.addImport("com.ericsson.deviceaccess.api.service." + category + "." + StringHelper.capitalize(name));

        builder.setJavadoc(new Javadoc(StringHelper.setEndPunctuation(service.getDescription())));
        builder.setName(StringHelper.capitalize(name) + "Base");
        builder.setExtends("SchemaBasedServiceBase");
        builder.addImplements(StringHelper.capitalize(name));
        builder.setClassModifier(ClassModifier.ABSTRACT);

        addConstructor(builder, service);
        addSetActionsResultsOnContextMethods(builder, service);
        addPropertyGettersAndUpdaters(builder, service);

        // Mandatory refresh properties action
        Method method = new Method("void", "refreshProperties").setJavadoc(new Javadoc("Refresh all properties."));
        method.setAccessModifier(AccessModifier.PROTECTED).addModifier(OptionalModifier.ABSTRACT);
        builder.addMethod(method);
    }

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
                    b.add("GenericDeviceProperties arguments = context.getArguments();");
                    b.add(getResultDecl(action) + "execute" + StringHelper.capitalize(name) + "(" + getGetArgumentsFromContext(action) + ");");
                    if (action.isSetResults()) {
                        b.add("set" + StringHelper.capitalize(name) + "ResultOnContext(context, result);");
                    }
                }).append(");");
            }
        }
    }

    private static String getResultDecl(Action action) {
        if (action.isSetResults()) {
            return StringHelper.capitalize(action.getName()) + "Result result = ";
        }
        return "";
    }

    private static String getGetArgumentsFromContext(Action action) {
        if (action.isSetArguments()) {
            StringBuilder signature = new StringBuilder();
            for (Parameter argument : action.getArguments().getParameterArray()) {
                signature.append("arguments.get")
                        .append(StringHelper.capitalize(StringHelper.getType(argument.getType())))
                        .append("Value(ACTION_").append(action.getName())
                        .append("_ARG_").append(argument.getName()).append("), ");
            }
            signature.setLength(signature.length() - 2);
            return signature.toString();
        }
        return "";
    }

    private static void addSetActionsResultsOnContextMethods(JavaClass builder, Service service) {
        if (service.isSetActions()) {
            for (Action action : service.getActions().getActionArray()) {
                String name = action.getName();
                if (action.isSetResults()) {
                    Method method = new Method("void", "set" + StringHelper.capitalize(name) + "ResultOnContext");
                    method.setJavadoc(new Javadoc("Sets the result from the '").append(name).append("' action on the specified context."));
                    method.setAccessModifier(AccessModifier.PRIVATE);
                    method.addParameter(new Param("GenericDeviceActionContext", "context").setDescription("action context to set result in"));
                    method.addParameter(new Param(StringHelper.capitalize(name) + "Result", "result").setDescription("result to set"));

                    method.add("GenericDeviceProperties value = #0.getResult().getValue();");
                    for (Parameter result : action.getResults().getParameterArray()) {
                        String type = StringHelper.capitalize(StringHelper.getType(result.getType()));
                        method.add("value.set" + type + "Value(ACTION_" + name + "_RES_" + result.getName() + ", #1." + result.getName() + ");");
                    }
                    builder.addMethod(method);
                }
            }
        }
    }

    private static void addPropertyGettersAndUpdaters(JavaClass builder, Service service) {
        if (service.isSetProperties()) {
            for (Parameter property : service.getProperties().getParameterArray()) {
                String name = property.getName();
                String type = property.getType();

                Method method = new Method(StringHelper.getType(type), "get" + StringHelper.capitalize(name));
                method.addModifier(OptionalModifier.FINAL);
                method.setJavadoc(new Javadoc().inherit());
                method.add("return getProperties().get" + StringHelper.capitalize(StringHelper.getType(type)) + "Value(PROP_" + name + ");");
                builder.addMethod(method);

                method = new Method("void", "update" + StringHelper.capitalize(name));
                method.setAccessModifier(AccessModifier.PROTECTED).addModifier(OptionalModifier.FINAL);
                method.setJavadoc(new Javadoc("Update the '").append(name).append("' property. To be used by concrete implementations of the service."));
                method.addParameter(new Param(StringHelper.getType(property.getType()), "value").setDescription("value to be set"));
                method.add("getProperties().set" + StringHelper.capitalize(StringHelper.getType(type)) + "Value(PROP_" + name + ", #0);");
                builder.addMethod(method);
            }
        }
    }
}
