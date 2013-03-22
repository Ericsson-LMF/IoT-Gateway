package com.ericsson.deviceaccess.serviceschema.codegenerator;

import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.service.xmlparser.ServiceSchemaDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServicesDocument.Services;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Main class of this application.
 */
public class Main {
    private ServiceSchemaDocument serviceSchemaDocument;

    public void run(File serviceSchemaXml, File outputBaseDir) throws XmlException, IOException {
        System.out.println("Starting code generation from " + serviceSchemaXml + " to " + outputBaseDir);

        serviceSchemaDocument = ServiceSchemaDocument.Factory.parse(serviceSchemaXml);
        File spiBaseDir = new File(outputBaseDir, "/com/ericsson/deviceaccess/spi/service");
        spiBaseDir.mkdirs();
        File apiBaseDir = new File(outputBaseDir, "/com/ericsson/deviceaccess/api/service");
        apiBaseDir.mkdirs();

        String version = serviceSchemaDocument.getServiceSchema().getVersion();
        Services services = serviceSchemaDocument.getServiceSchema().getServices();
        ServicePrinter sp = new ServicePrinter();
        Service[] serviceArray = (Service[]) services.getServiceArray();
        // Generate schema definition
        File schemaDefSourceFile = new File(spiBaseDir, "SchemaDefinitions.java");
        PrintStream schemaDefStream = new PrintStream(schemaDefSourceFile);
        sp.printSchemaDefinitionStart(schemaDefStream, version);
        for (Service service : serviceArray) {

            sp.printSchemaDefinitionForService(schemaDefStream, service);

            PrintStream spiPrintStream = null;
            PrintStream apiPrintStream = null;
            File spiPackageDir = new File(spiBaseDir, makePath(service));
            spiPackageDir.mkdirs();
            File spiSourceFile = new File(spiPackageDir, capitalize(service.getName() + "Base") + ".java");
            spiPrintStream = new PrintStream(spiSourceFile);
            sp.printServiceImpl(spiPrintStream, version, service);
            spiPrintStream.close();
            System.out.printf("Generated SPI base for '%s' to %s\n", service.getName(), spiSourceFile.getAbsolutePath());

            File apiPackageDir = new File(apiBaseDir, makePath(service));
            apiPackageDir.mkdirs();
            File apiSourceFile = new File(apiPackageDir, capitalize(service.getName()) + ".java");
            apiPrintStream = new PrintStream(apiSourceFile);
            sp.printServiceInterface(apiPrintStream, version, service);
            apiPrintStream.close();
            System.out.printf("Generated API for '%s' to %s\n", service.getName(), apiSourceFile.getAbsolutePath());
        }

        sp.printSchemaDefinitionEnd(schemaDefStream);
        schemaDefStream.close();

        System.out.println("Code generation completed!");
    }

    private String makePath(Service service) {
        String category = service.getCategory();
        if (category == null || category.length() == 0) {
            throw new IllegalArgumentException("The category must be specified for service: "+service.getName());
        } else {
            return category.replace('.', '/');
        }
    }

    private String capitalize(String string) {
        StringBuffer sb = new StringBuffer(string);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static void main(String[] args) throws XmlException, IOException {
        if (args.length < 2) {
            System.out.println("The schema file AND the output base directory must be specified");
            System.exit(1);
        }

        Main main = new Main();
        main.run(new File(args[0]), new File(args[1]));
    }
}
