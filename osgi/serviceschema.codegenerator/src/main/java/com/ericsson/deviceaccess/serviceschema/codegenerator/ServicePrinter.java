/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 *
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,

 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ActionDocument.Action;
import com.ericsson.deviceaccess.service.xmlparser.ParameterDocument.Parameter;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.service.xmlparser.ServiceSchemaDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServicesDocument.Services;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlock;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaClass;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Javadoc;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.xmlbeans.XmlException;

public class ServicePrinter {

    public static void main(String[] args) throws XmlException, IOException {
        ServiceSchemaDocument serviceSchemaDocument = ServiceSchemaDocument.Factory.parse(new File("src/test/resources/services-example.xml"));
        String version = serviceSchemaDocument.getServiceSchema().getVersion();
        Services services = serviceSchemaDocument.getServiceSchema().getServices();
        ServicePrinter sp = new ServicePrinter();
        Service[] serviceArray = services.getServiceArray();
        
        JavaClass builder = new JavaClass();
        CodeBlock code = DefinitionsAdder.addDefinitionsStart(builder);
        for (Service service : serviceArray) {
            DefinitionsAdder.addService(code, service);
        }
//        System.out.print(builder.build(ServicePrinter.class));
        
        System.out.println("=============================================");
        System.out.println("=============================================");
        System.out.println("=============================================");
        System.out.println("=============================================");
        
        for (Service service : serviceArray) {
            System.out.println("#############################################");
            builder = new JavaClass();
            InterfaceAdder.addServiceInterface(builder, version, service);
            System.out.print(builder.build(ServicePrinter.class));
            System.out.println("=============================================");
            System.out.println("=============================================");
            
        }
        System.out.println("=============================================");
        
        for (Service service : serviceArray) {
//            sp.printServiceInterface(System.out, version, service);
            System.out.println("=============================================");
            sp.printServiceImpl(System.out, version, service);
            System.out.println("=============================================");
            System.out.println("=============================================");
        }
    }

    private final String GENERATION_WARNING = "THIS IS AUTOMATICALLY GENERATED BY {@link " + ServicePrinter.class.getCanonicalName() + "}.";

    public void printServiceImpl(PrintStream out, String version, Service service) {
        String name = service.getName();
        out.printf("package com.ericsson.deviceaccess.spi.service.%s;\n", service.getCategory());
        out.println();
        out.printf("import com.ericsson.deviceaccess.spi.schema.SchemaBasedServiceBase;\n");
        out.printf("import com.ericsson.deviceaccess.spi.service.SchemaDefinitions;\n");
        out.printf("import com.ericsson.deviceaccess.api.GenericDeviceActionContext;\n");
        out.printf("import com.ericsson.deviceaccess.api.GenericDeviceException;\n");
        out.printf("import com.ericsson.deviceaccess.spi.schema.ActionDefinition;\n");
        out.printf("import com.ericsson.deviceaccess.api.service.%s.%s;\n", service.getCategory(), StringHelper.capitalize(service.getName()));
        out.println();
        out.print(new Javadoc(GENERATION_WARNING).line(StringHelper.setEndPunctuation(service.getDescription())));
        out.printf("public abstract class %sBase extends SchemaBasedServiceBase implements %s {\n", StringHelper.capitalize(name), StringHelper.capitalize(name));
        printConstructor(service, out);
        printSetActionsResultsOnContextMethods(service, out);
        printPropertyGettersAndUpdaters(service, out);
        // Mandatory refresh properties action
        out.println();
        out.print(new Javadoc("Refresh all properties."));
        out.println("  protected abstract void refreshProperties();\n");

        out.printf("}\n", StringHelper.capitalize(name));
    }

    
    
    private void printConstructor(Service service, PrintStream out) {
        out.print(new Javadoc("Creates the service and maps actions to methods that shall be defined by subclass."));
        out.printf("  protected %sBase(){\n", StringHelper.capitalize(service.getName()));
        out.println("    super(SchemaDefinitions.INSTANCE.getServiceSchema(SERVICE_NAME));");

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
                out.printf("        %sexecute%s(%s);\n", getResultDecl(action), StringHelper.capitalize(action.getName()), getGetArgumentsFromContext(action));
                if (action.getResults() != null && action.getResults().getParameterArray().length != 0) {
                    out.printf("        set%sResultOnContext(context, result);\n", StringHelper.capitalize(action.getName()));
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
                signature.append("context.getArguments().get")
                        .append(StringHelper.capitalize(StringHelper.getType(argument.getType())))
                        .append("Value(ACTION_").append(action.getName())
                        .append("_ARG_").append(argument.getName()).append("), ");
            }
            signature.setLength(signature.length() - 2);
            return signature.toString();
        }
        return "";
    }
    private void printSetActionsResultsOnContextMethods(Service service, PrintStream out) {
        if (service.getActions() != null) {
            for (Action action : service.getActions().getActionArray()) {
                if (action.getResults() != null && action.getResults().getParameterArray().length > 0) {
                    out.println();
                    out.print(new Javadoc("Sets the result from the '").append(action.getName()).append("' action on the specified context."));
                    out.printf("  private final void set%sResultOnContext(GenericDeviceActionContext context, %sResult result) {\n", StringHelper.capitalize(action.getName()), StringHelper.capitalize(action.getName()));
                    for (Parameter result : action.getResults().getParameterArray()) {
                        out.printf("    context.getResult().getValue().set%sValue(ACTION_%s_RES_%s, result.%s);\n", StringHelper.capitalize(StringHelper.getType(result.getType())), action.getName(), result.getName(), result.getName());
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
                out.print(new Javadoc().inherit());
                out.printf("  public final %s {\n", getterSignature);
                out.printf("    return getProperties().get%sValue(PROP_%s);\n", StringHelper.capitalize(StringHelper.getType(property.getType())), property.getName());
                out.printf("  }\n");
                out.println();

                String updaterSignature = getPropertyUpdateSignature(property);
                out.print(new Javadoc("Update the '").append(property.getName()).append("' property. To be used by concrete implementations of the service."));
                out.printf("  protected final %s {\n", updaterSignature);
                out.printf("    getProperties().set%sValue(PROP_%s, value);\n", StringHelper.capitalize(StringHelper.getType(property.getType())), property.getName());
                out.printf("  }\n");
            }
        }
    }

    private String getResultDecl(Action action) {
        if (action.getResults() != null && action.getResults().getParameterArray().length != 0) {
            return StringHelper.capitalize(action.getName()) + "Result result = ";
        }
        return "";
    }


    private String getPropertyGetterSignature(Parameter property) {
        return StringHelper.getType(property.getType()) + " get" + StringHelper.capitalize(property.getName()) + "()";
    }

    private String getPropertyUpdateSignature(Parameter property) {
        return "void update" + StringHelper.capitalize(property.getName()) + "(" + StringHelper.getType(property.getType()) + " value)";
    }

}
