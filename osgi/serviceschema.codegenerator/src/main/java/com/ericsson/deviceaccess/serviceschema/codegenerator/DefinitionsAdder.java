package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ActionDocument;
import com.ericsson.deviceaccess.service.xmlparser.ActionsDocument.Actions;
import com.ericsson.deviceaccess.service.xmlparser.ArgumentsDocument;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter;
import com.ericsson.deviceaccess.service.xmlparser.PropertiesDocument.Properties;
import com.ericsson.deviceaccess.service.xmlparser.ResultsDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.service.xmlparser.ValuesDocument;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlock;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Constructor;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaClass;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Javadoc;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Method;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Variable;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.ClassModifier;

/**
 *
 * @author delma
 */
public enum DefinitionsAdder {

    INSTANCE;

    private static final String SERVICE_SB = "service";
    private static final String PARAMETER_SB = "parameter";
    private static final String ACTION_SB = "action";

    public static CodeBlock addDefinitionsStart(JavaClass builder) {
        builder.setPackage("com.ericsson.deviceaccess.spi.service");
        builder.addImport("java.util.HashMap");
        builder.addImport("java.util.Map");
        builder.addImport("com.ericsson.deviceaccess.spi.schema.ServiceSchema");
        builder.addImport("com.ericsson.deviceaccess.spi.schema.ActionSchema");
        builder.addImport("com.ericsson.deviceaccess.spi.schema.ParameterSchema");
        builder.addImport("com.ericsson.deviceaccess.spi.schema.ServiceSchemaError");
        builder.setClassModifier(ClassModifier.SINGLETON);
        builder.setName("SchemaDefinitions");
        builder.setJavadoc(new Javadoc("Defines service schemata"));
        builder.addVariable(new Variable("Map<String, ServiceSchema>", "serviceSchemas").init("new HashMap<>()"));
        Constructor code = new Constructor().setJavadoc(new Javadoc("Constructor which generates schemata."));
        builder.addConstructor(code);
        code.add("ActionSchema.Builder ").append(ACTION_SB).append(";");
        code.add("ParameterSchema.Builder ").append(PARAMETER_SB).append(";");
        code.add("ServiceSchema.Builder ").append(SERVICE_SB).append(";");
        builder.addMethod(new Method("ServiceSchema", "getServiceSchema").setJavadoc(new Javadoc("Gets ServiceSchema based on it's name.").result("Service schema")).addParameter("String", "name", "name of schema").add("return serviceSchemas.get(#0);"));
        return code;
    }

    public static void addService(CodeBlock code, Service service) {
        code.add("");
        String name = service.getName();
        code.add("//CREATING SCHEMA FOR: ").append(name);
        code.add(SERVICE_SB).append(" = new ServiceSchema.Builder(\"").append(name).append("\");");
        addActions(code, service.getActions());
        addProperties(code, service.getProperties());
        code.add("serviceSchemas.put(\"").append(name).append("\", ").append(SERVICE_SB).append(".build());");
    }

    private static void addCreateParameterBuilder(CodeBlock code, String name, String type, String defaultValue) {
        code.add(PARAMETER_SB).append(" = new ParameterSchema.Builder(\"").append(name).append("\", ").append(type).append(".class);");
        if (defaultValue != null) {
            code.add(PARAMETER_SB).append(".setDefaultValue(\"").append(defaultValue).append("\");");
        }
    }

    /**
     * @param out
     * @param action
     */
    private static void addParameter(CodeBlock code, Parameter parameter) {
        String name = parameter.getName();
        String type = parameter.getType();
        String default0 = parameter.getDefault();
        if ("String".equals(StringHelper.getType(type))) {
            ValuesDocument.Values values = parameter.getValues();
            if (default0 == null && parameter.isSetValues()) {
                default0 = values.getValueArray(0);
            }
            addCreateParameterBuilder(code, name, type, default0);
            if (parameter.isSetValues()) {
                code.add(PARAMETER_SB).append(".setValidValues(new String[]{");
                for (String value : values.getValueArray()) {
                    code.add("\"" + value + "\",");
                }
                code.add("});");
            }
        } else {
            addCreateParameterBuilder(code, name, type, default0);
            if (parameter.isSetMin()) {
                code.add(PARAMETER_SB).append(".setMinValue(\"").append(parameter.getMin()).append("\");");
            }
            if (parameter.isSetMax()) {
                code.add(PARAMETER_SB).append(".setMaxValue(\"").append(parameter.getMax()).append("\");");
            }
        }
    }

    private static void addActions(CodeBlock code, Actions actions) {
        if (actions == null) {
            return;
        }
        for (ActionDocument.Action action : actions.getActionArray()) {
            String name = action.getName();
            boolean mandatory = !action.getOptional();
            code.add(ACTION_SB).append(" = new ActionSchema.Builder(\"").append(name).append("\").setMandatory(").append(mandatory).append(");");
            ArgumentsDocument.Arguments arguments = action.getArguments();
            if (arguments != null) {
                for (ParameterDocument.Parameter argument : arguments.getParameterArray()) {
                    addParameter(code, argument);
                    code.add(ACTION_SB).append(".addArgumentSchema(").append(PARAMETER_SB).append(".build());");
                }
            }
            ResultsDocument.Results results = action.getResults();
            if (results != null) {
                for (ParameterDocument.Parameter result : results.getParameterArray()) {
                    addParameter(code, result);
                    code.add(ACTION_SB).append(".addResultSchema(").append(PARAMETER_SB).append(".build());");
                }
            }
            code.add(SERVICE_SB).append(".addActionSchema(").append(ACTION_SB).append(".build());");
        }
    }

    private static void addProperties(CodeBlock code, Properties properties) {
        if (properties == null) {
            return;
        }
        for (ParameterDocument.Parameter property : properties.getParameterArray()) {
            addParameter(code, property);
            code.add(SERVICE_SB).append(".addPropertySchema(").append(PARAMETER_SB).append(".build());");
        }
    }

}
