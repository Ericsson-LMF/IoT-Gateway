package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ActionDocument.Action;
import com.ericsson.deviceaccess.service.xmlparser.ActionsDocument.Actions;
import com.ericsson.deviceaccess.service.xmlparser.ArgumentsDocument.Arguments;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter;
import com.ericsson.deviceaccess.service.xmlparser.PropertiesDocument.Properties;
import com.ericsson.deviceaccess.service.xmlparser.ResultsDocument.Results;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.service.xmlparser.ServiceSchemaDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServicesDocument.Services;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ServicePrinter {
    /**
     * @param schemaDefStream
     * @param version
     */
    public void printSchemaDefinitionStart(PrintStream out, String version) {
        out.println("package com.ericsson.deviceaccess.spi.service;");
        out.println();
        out.println("import java.util.HashMap;");
        out.println("import java.util.Map;");
        out.println();
        out.println("import com.ericsson.deviceaccess.spi.schema.ServiceSchema;");
        out.println("import com.ericsson.deviceaccess.spi.schema.ActionSchema;");
        out.println("import com.ericsson.deviceaccess.spi.schema.ParameterSchema;");
        out.println("import com.ericsson.deviceaccess.spi.schema.ServiceSchemaError;");
        out.println();
        out.println("public class SchemaDefinitions {");
        out.println("  private static SchemaDefinitions instance = new SchemaDefinitions();");
        out.println("  private Map serviceSchemas = new HashMap();");
        out.println();
        out.println("  private SchemaDefinitions() {}");
        out.println();
        out.println("  public ServiceSchema getServiceSchema(String name) {");
        out.println("    return (ServiceSchema) serviceSchemas.get(name);");
        out.println("  }");
        out.println();
        out.println("  public static SchemaDefinitions getInstance() {");
        out.println("    return instance;");
        out.println("  }");
        out.println();
        out.println("  static {");
        out.println("    ActionSchema.Builder actionSchemaBuilder = null;");
        out.println("    ParameterSchema.Builder parameterSchemaBuilder = null;");
        out.println("    ServiceSchema.Builder serviceSchemaBuilder = null;");
    }

    /**
     * @param schemaDefStream
     * @param version
     * @param service
     */
    public void printSchemaDefinitionForService(PrintStream out, Service service) {
        out.println();
        out.printf("    serviceSchemaBuilder = new ServiceSchema.Builder(\"%s\");\n", service.getName());
        printActionSchemasCreation(out, service);
        printPropertySchemasCreation(out, service);
        out.printf("    getInstance().serviceSchemas.put(\"%s\", serviceSchemaBuilder.build());\n", service.getName());
    }

    /**
     * @param out
     * @param service
     */
    private void printActionSchemasCreation(PrintStream out, Service service) {
        if (service.getActions() != null && service.getActions().getActionArray().length > 0) {
            for (Action action : service.getActions().getActionArray()) {
                out.printf("    actionSchemaBuilder = new ActionSchema.Builder(\"%s\").setMandatory(%s);\n", action.getName(), !action.getOptional());
                if (action.getArguments() != null && action.getArguments().getParameterArray().length > 0) {
                    for (Parameter argument : action.getArguments().getParameterArray()) {
                        printParameterSchemaCreation(out, argument);
                        out.printf("    actionSchemaBuilder.addArgumentSchema(parameterSchemaBuilder.build());\n");
                    }
                }
                if (action.getResults() != null && action.getResults().getParameterArray().length > 0) {
                    for (Parameter result : action.getResults().getParameterArray()) {
                        printParameterSchemaCreation(out, result);
                        out.printf("    actionSchemaBuilder.addResultSchema(parameterSchemaBuilder.build());\n");
                    }
                }
                out.printf("    serviceSchemaBuilder.addActionSchema(actionSchemaBuilder.build());\n");
            }
        }
    }

    /**
     * @param out
     * @param action
     */
    private void printParameterSchemaCreation(PrintStream out, Parameter parameter) {
        if ("String".equals(getType(parameter.getType()))) {
            if (parameter.getValues() != null && parameter.getValues().getValueArray().length > 0) {
                out.printf("    parameterSchemaBuilder = new ParameterSchema.Builder(\"%s\").setType(String.class).setDefaultValue(\"%s\");\n",
                        parameter.getName(),
                        (parameter.getDefault() != null ?
                                parameter.getDefault() :
                                parameter.getValues().getValueArray()[0]));
                out.printf("    parameterSchemaBuilder.setValidValues(new String[]{\n");
                for (String value : parameter.getValues().getValueArray()) {
                    out.printf("      \"%s\",\n", value);
                }
                out.printf("    });\n");
            } else {
                out.printf("    parameterSchemaBuilder = new ParameterSchema.Builder(\"%s\").setType(String.class).setDefaultValue(\"%s\");\n",
                        parameter.getName(), (parameter.getDefault() != null ? parameter.getDefault() : ""));
            }
        } else {
            out.printf("    parameterSchemaBuilder = new ParameterSchema.Builder(\"%s\").setType(%s.class).setDefaultValue(new %s(%s));\n",
                    parameter.getName(), parameter.getType(), parameter.getType(), (parameter.getDefault() != null ? parameter.getDefault() : "0"));
            if (parameter.getMin() != null) {
                out.printf("    parameterSchemaBuilder.setMinValue(\"%s\");\n", parameter.getMin());
            }
            if (parameter.getMax() != null) {
                out.printf("    parameterSchemaBuilder.setMaxValue(\"%s\");\n", parameter.getMax());
            }
        }
    }

    /**
     * @param out
     * @param service
     */
    private void printPropertySchemasCreation(PrintStream out, Service service) {
        if (service.getProperties() != null && service.getProperties().getParameterArray().length > 0) {
            for (Parameter property : service.getProperties().getParameterArray()) {
                printParameterSchemaCreation(out, property);
                out.printf("    serviceSchemaBuilder.addPropertySchema(parameterSchemaBuilder.build());\n");
            }
        }
    }

    /**
     * @param schemaDefStream
     */
    public void printSchemaDefinitionEnd(PrintStream out) {
        out.println();
        out.println("  }");
        out.println("}");
    }

    public void printServiceImpl(PrintStream out, String version, Service service) {
        String name = service.getName();
        out.printf("package com.ericsson.deviceaccess.spi.service.%s;\n", service.getCategory());
        out.println();
        out.printf("import com.ericsson.deviceaccess.spi.schema.SchemaBasedServiceBase;\n");
        out.printf("import com.ericsson.deviceaccess.spi.service.SchemaDefinitions;\n");
        out.printf("import com.ericsson.deviceaccess.api.GenericDeviceActionContext;\n");
        out.printf("import com.ericsson.deviceaccess.api.GenericDeviceException;\n");
        out.printf("import com.ericsson.deviceaccess.spi.schema.ActionDefinition;\n");
        out.printf("import com.ericsson.deviceaccess.api.service.%s.%s;\n", service.getCategory(), capitalize(service.getName()));
        out.println();
        out.printf("/**\n * %s\n */\n", setEndPunctuation(service.getDescription()));
        out.printf("public abstract class %sBase extends SchemaBasedServiceBase implements %s {\n", capitalize(name), capitalize(name));
        printConstructor(service, out);
        printSetActionsResultsOnContextMethods(service, out);
        printPropertyGettersAndUpdaters(service, out);
        // Mandatory refresh properties action
        out.println("\n  /**\n   * Refresh all properties. \n   */");
        out.println("  protected abstract void refreshProperties();\n");

        out.printf("}\n", capitalize(name));
    }

    public void printServiceInterface(PrintStream out, String version, Service service) {
        String name = service.getName();
        out.printf("package com.ericsson.deviceaccess.api.service.%s;\n", service.getCategory());
        out.println();
        out.printf("import com.ericsson.deviceaccess.api.GenericDeviceService;\n");
        out.printf("import com.ericsson.deviceaccess.api.GenericDeviceException;\n");
        out.println();
        out.printf("/**\n * %s\n */\n", setEndPunctuation(service.getDescription()));
        out.printf("public interface %s extends GenericDeviceService {\n", capitalize(name));
        out.printf("  public static final String SCHEMA_VERSION=\"%s\";\n", version);
        printConstants(service, out);
        printPropertyGetters(service, out);
        printActionDefinitions(service, out);
        printActionsResultTypes(service, out);
        out.printf("}\n", capitalize(name));
    }

    private void printConstants(Service service, PrintStream out) {
        out.printf("  public static final String SERVICE_NAME = \"%s\";\n", service.getName());
        Actions actions = service.getActions();
        // Mandatory refresh properties action
        out.printf("  public static final String ACTION_refreshProperties = \"refreshProperties\";\n");
        if (actions != null) {
            for (Action action : actions.getActionArray()) {
                out.printf("  public static final String ACTION_%s = \"%s\";\n", action.getName(), action.getName());
                Arguments arguments = action.getArguments();
                if (arguments != null) {
                    printParameterConstants(out, arguments.getParameterArray(), "ACTION_" + action.getName() + "_ARG");
                }

                Results results = action.getResults();
                if (results != null) {
                    printParameterConstants(out, results.getParameterArray(),  "ACTION_" + action.getName() + "_RES");
                }
            }
        }

        // Mandatory last update time property
        out.printf("  public static final String PROP_lastUpdateTime = \"lastUpdateTime\";\n");
        Properties properties = service.getProperties();
        if (properties != null) {
            printParameterConstants(out, properties.getParameterArray(),  "PROP");
        }
    }

    /**
     * @param out
     * @param arguments
     */
    private void printParameterConstants(PrintStream out, Parameter[] parameterArray, String prefix) {
        for (Parameter parameter : parameterArray) {
            out.printf("  public static final String " + prefix + "_%s = \"%s\";\n", parameter.getName(), parameter.getName());
            if (parameter.getValues() != null && parameter.getValues().getValueArray().length > 0) {
                for (String value : parameter.getValues().getValueArray()) {
                    out.printf("  public static final String VALUE_" + prefix + "_%s_%s = \"%s\";\n", parameter.getName(), value, value);
                }
            }
        }
    }

    private void printConstructor(Service service, PrintStream out) {
        out.printf("  /**\n   * Creates the service and maps actions to methods that shall be defined by subclass.\n   */\n");
        out.printf("  protected %sBase(){\n", capitalize(service.getName()));
        out.println("    super(SchemaDefinitions.getInstance().getServiceSchema(SERVICE_NAME));");

        // Mandatory refresh properties action
        out.printf("    defineAction(ACTION_refreshProperties, new ActionDefinition() {\n");
        out.printf("      public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {\n");
        out.printf("        if (!context.isAuthorized()) return;\n");
        out.printf("        refreshProperties();\n");
        out.printf("      }\n");
        out.printf("    });\n");

        if (service.getActions() != null) {
            for (Action action : service.getActions().getActionArray()) {
                out.printf("    defineAction(ACTION_%s, new ActionDefinition() {\n", action.getName());
                out.printf("      public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {\n");
                out.printf("        if (!context.isAuthorized()) return;\n");
                out.printf("        %sexecute%s(%s);\n", getResultDecl(action), capitalize(action.getName()), getGetArgumentsFromContext(action));
                if (action.getResults() != null && action.getResults().getParameterArray().length != 0) {
                    out.printf("        set%sResultOnContext(context, result);\n", capitalize(action.getName()));
                }
                out.printf("      }\n");
                out.printf("    });\n");
            }
        }
        out.printf("  }\n");
    }

    private String getGetArgumentsFromContext(Action action) {
        if (action.getArguments() != null && action.getArguments().getParameterArray().length > 0) {
            StringBuilder signature = new StringBuilder();
            for (Parameter argument : action.getArguments().getParameterArray()) {
                signature.append("context.getArguments().get" + capitalize(getType(argument.getType())) + "Value(ACTION_"+action.getName()+"_ARG_" + argument.getName() + "), ");
            }
            signature.setLength(signature.length() - 2);
            return signature.toString();
        }
        return "";
    }

    private void printActionsResultTypes(Service service, PrintStream out) {
        if (service.getActions() != null) {
            for (Action action : service.getActions().getActionArray()) {
                if (action.getResults() != null && action.getResults().getParameterArray().length > 0) {
                    out.println();
                    out.printf("  /**\n   * Result from action '%s'.\n   */\n", action.getName());
                    out.printf("  public final static class %sResult {\n", capitalize(action.getName()));
                    for (Parameter result : action.getResults().getParameterArray()) {
                        out.printf("    /**\n     * %s\n     */\n", setEndPunctuation(result.getDescription()));
                        out.printf("    public %s %s;\n", getType(result.getType()), result.getName());
                    }
                    out.printf("  }\n");
                }
            }
        }
    }

    private void printSetActionsResultsOnContextMethods(Service service, PrintStream out) {
        if (service.getActions() != null) {
            for (Action action : service.getActions().getActionArray()) {
                if (action.getResults() != null && action.getResults().getParameterArray().length > 0) {
                    out.println();
                    out.printf("  /**\n   * Sets the result from the '%s' action on the specified context.\n   */\n", action.getName());
                    out.printf("  private final void set%sResultOnContext(GenericDeviceActionContext context, %sResult result) {\n", capitalize(action.getName()), capitalize(action.getName()));
                    for (Parameter result : action.getResults().getParameterArray()) {
                        out.printf("    context.getResult().getValue().set%sValue(ACTION_%s_RES_%s, result.%s);\n", capitalize(getType(result.getType())), action.getName(), result.getName(), result.getName());
                    }

                    out.printf("  }\n");
                }
            }
        }
    }

    private void printPropertyGettersAndUpdaters(Service service, PrintStream out) {
        if (service.getProperties() != null && service.getProperties().getParameterArray().length > 0) {
            for (Parameter property : service.getProperties().getParameterArray()) {
                out.println();
                String getterSignature = getPropertyGetterSignature(property);
                out.printf("  /**\n   * {@inheritDoc}\n   */\n");
                out.printf("  public final %s {\n", getterSignature);
                out.printf("    return getProperties().get%sValue(PROP_%s);\n", capitalize(getType(property.getType())), property.getName());
                out.printf("  }\n");
                out.println();

                String updaterSignature = getPropertyUpdateSignature(property);
                out.printf("  /**\n   * Update the '%s' property. To be used by concrete implementations of the service.\n   */\n", property.getName());
                out.printf("  protected final %s {\n", updaterSignature);
                out.printf("    getProperties().set%sValue(PROP_%s, value);\n", capitalize(getType(property.getType())), property.getName());
                out.printf("  }\n");
            }
        }
    }

    private void printPropertyGetters(Service service, PrintStream out) {
        if (service.getProperties() != null && service.getProperties().getParameterArray().length > 0) {
            for (Parameter property : service.getProperties().getParameterArray()) {
                out.println();
                String getterSignature = getPropertyGetterSignature(property);
                out.printf("  /**\n   * Gets the property '%s'.\n   *\n   * Property description: %s\n%s   */\n", property.getName(), setEndPunctuation(property.getDescription()), getValidValuesJavadoc(property));
                out.printf("  %s;\n", getterSignature);
            }
        }
    }

    private String getValidValuesJavadoc(Parameter property) {
        if ("String".equals(getType(property.getType()))) {
            if (property.getValues() != null && property.getValues().getValueArray().length > 0) {
                return "   *\n   * Valid values:\n" + getValidValuesString(property.getValues().getValueArray()) + "\n";
            }
        } else if (property.getMin() != null || property.getMax() != null) {
            return "   *\n" + (property.getMin() != null ? "   * Min: " + property.getMin() + "\n" : "") + (property.getMax() != null ? "   * Max: " + property.getMax() + "\n" : "");
        }
        return "";
    }

    private String getValidValuesString(String[] valueArray) {
        String retVal = "   * <ul>\n";
        for (String value : valueArray) {
            retVal += "   *   <li>\"" + value + "\"</li>\n";
        }

        return retVal + "   * </ul>";
    }

    private void printActionDefinitions(Service service, PrintStream out) {
        if (service.getActions() != null) {
            for (Action action : service.getActions().getActionArray()) {
                String result = "void";
                if (action.getResults() != null && action.getResults().getParameterArray().length > 0) {
                    result = capitalize(action.getName()) + "Result";
                }
                out.println();
                out.printf("  /**\n   * Execute the action '%s'.\n   *\n   * Action description: %s\n", action.getName(), action.getDescription());
                if (action.getArguments() != null && action.getArguments().getParameterArray().length > 0) {
                    out.println("   *");
                    for (Parameter arg : action.getArguments().getParameterArray()) {
                        out.printf("   * @param %s %s\n", arg.getName(), arg.getDescription());
                    }
                }

                if (action.getResults() != null) {
                    out.println("   *");
                    out.printf("   * @return {@link %sResult}\n", capitalize(action.getName()));
                }
                out.printf("   */\n");
                out.printf("  %s execute%s(%s) throws GenericDeviceException;\n", result, capitalize(action.getName()), getSignature(action));
            }
        }
    }

    private String getResultDecl(Action action) {
        if (action.getResults() != null && action.getResults().getParameterArray().length != 0) {
            return capitalize(action.getName()) + "Result result = ";
        }
        return "";
    }

    private String getSignature(Action action) {
        if (action.getArguments() != null && action.getArguments().getParameterArray().length > 0) {
            StringBuilder signature = new StringBuilder();
            for (com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter argument : action.getArguments().getParameterArray()) {
                signature.append(getType(argument.getType()) + " " + argument.getName() + ", ");
            }
            signature.setLength(signature.length() - 2);
            return signature.toString();
        }
        return "";
    }

    private String getType(String type) {
        if (type.toLowerCase().startsWith("int")) {
            return "int";
        } else if (type.toLowerCase().startsWith("float")) {
            return "float";
        } else {
            return "String";
        }
    }

    private String capitalize(String string) {
        StringBuffer sb = new StringBuffer(string);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    private String getPropertyGetterSignature(Parameter property) {
        return getType(property.getType()) + " get" + capitalize(property.getName()) + "()";
    }

    private String getPropertyUpdateSignature(Parameter property) {
        return "void update" + capitalize(property.getName()) + "(" + getType(property.getType()) + " value)";
    }

    private String setEndPunctuation(String description) {
        if (!description.endsWith(".")) {
            return description + ".";
        }
        return description;
    }

    public static void main(String[] args) throws XmlException, IOException {
        ServiceSchemaDocument serviceSchemaDocument = ServiceSchemaDocument.Factory.parse(new File("src/test/resources/services-example.xml"));
        String version = serviceSchemaDocument.getServiceSchema().getVersion();
        Services services = serviceSchemaDocument.getServiceSchema().getServices();
        ServicePrinter sp = new ServicePrinter();
        Service[] serviceArray = services.getServiceArray();

        sp.printSchemaDefinitionStart(System.out, version);
        for (Service service : serviceArray) {
            sp.printSchemaDefinitionForService(System.out, service);
        }
        sp.printSchemaDefinitionEnd(System.out);
        System.out.println("=============================================");
        System.out.println("=============================================");
        System.out.println("=============================================");
        System.out.println("=============================================");

        for (Service service : serviceArray) {
            sp.printServiceInterface(System.out, version, service);
            System.out.println("=============================================");
            sp.printServiceImpl(System.out, version, service);
            System.out.println("=============================================");
            System.out.println("=============================================");
        }

    }
}
