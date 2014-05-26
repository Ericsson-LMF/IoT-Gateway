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

import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.service.xmlparser.ServiceSchemaDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServicesDocument.Services;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.xmlbeans.XmlException;

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
        Service[] serviceArray = services.getServiceArray();
        // Generate schema definition
        File schemaDefSourceFile = new File(spiBaseDir, "SchemaDefinitions.java");
        try (PrintStream schemaDefStream = new PrintStream(schemaDefSourceFile)) {
            sp.printSchemaDefinitionStart(schemaDefStream, version);
            for (Service service : serviceArray) {
                
                sp.printSchemaDefinitionForService(schemaDefStream, service);
                
                PrintStream spiPrintStream;
                PrintStream apiPrintStream;
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
        }

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
        StringBuilder sb = new StringBuilder(string);
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
