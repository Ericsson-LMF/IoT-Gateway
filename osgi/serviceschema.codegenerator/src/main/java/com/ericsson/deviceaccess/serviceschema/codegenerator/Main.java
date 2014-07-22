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

import com.ericsson.commonutil.StringUtil;
import com.ericsson.commonutil.function.TriConsumer;
import com.ericsson.deviceaccess.service.xmlparser.ServiceDocument.Service;
import com.ericsson.deviceaccess.service.xmlparser.ServiceSchemaDocument;
import com.ericsson.deviceaccess.service.xmlparser.ServicesDocument.Services;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.CodeBlock;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.JavaClass;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class that is used to generate Java code from schema XML file.
 */
public class Main implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DIRECTORY = "osgi";

    /**
     * Runs the code generation between xml file in first argument and output
     * directory in second argument
     *
     * @param args xmlFile outputDirectory
     * @throws XmlException
     * @throws IOException
     */
    public static void main(String[] args) throws XmlException, IOException {
        if (args.length < 2) {
            System.out.println("The schema file AND the output base directory must be specified");
            System.exit(1);
        }
        Main main = new Main(args[0], args[1]);
        main.run();
    }

    private ServiceSchemaDocument serviceSchemaDocument;
    private final String output;
    private final String input;
    private final File serviceSchemaXml;
    private final File outputBaseDir;

    /**
     * @param serviceSchemaXml xml file to read from
     * @param outputBaseDir java file to write to
     */
    private Main(String input, String output) {
        this.output = parsePackageName(output);
        this.input = parsePackageName(input);
        serviceSchemaXml = new File(input);
        outputBaseDir = new File(output);
    }

    /**
     * Runs code generation from schema XML to java code.
     */
    @Override
    public void run() {
        try {
            System.out.println("Starting code generation from " + serviceSchemaXml + " to " + outputBaseDir);

            serviceSchemaDocument = ServiceSchemaDocument.Factory.parse(serviceSchemaXml);
            File spiDirBase = new File(outputBaseDir, "/com/ericsson/deviceaccess/spi/service");
            spiDirBase.mkdirs();
            File apiDirBase = new File(outputBaseDir, "/com/ericsson/deviceaccess/api/service");
            apiDirBase.mkdirs();

            String version = serviceSchemaDocument.getServiceSchema().getVersion();
            Services services = serviceSchemaDocument.getServiceSchema().getServices();
            // Generate schema definition
            try (PrintStream schemaDefStream = new PrintStream(new File(spiDirBase, "SchemaDefinitions.java"))) {
                JavaClass builder = new JavaClass(input);
                CodeBlock code = DefinitionsAdder.addDefinitionsStart(builder);
                for (Service service : services.getServiceArray()) {
                    DefinitionsAdder.addService(code, service);
                    createFile("SPI", spiDirBase, "Base", service, version, ImplementationAdder::addServiceImplementation);
                    createFile("API", apiDirBase, "", service, version, InterfaceAdder::addServiceInterface);
                }
                schemaDefStream.append(builder.build());
            }

            System.out.println("Code generation completed!");
        } catch (XmlException | IOException ex) {
            logger.error("Exception: " + ex);
        }
    }

    /**
     * Creates file for API/SPI.
     *
     * @param baseDir Base directory under which files are contained
     * @param postfix postfix for file names
     * @param service service which file is added
     * @param version version of schema
     * @param generator generator which generates the code
     * @param what description what is generated
     * @throws IOException
     */
    private void createFile(String what, File baseDir, String postfix, Service service, String version, TriConsumer<JavaClass, Service, String> generator) throws IOException {
        File packageDir = new File(baseDir, makePath(service));
        packageDir.mkdirs();
        File sourceFile = new File(packageDir, StringUtil.capitalize(service.getName() + postfix) + ".java");
        try (PrintStream stream = new PrintStream(sourceFile)) {
            JavaClass builder = new JavaClass(input);
            generator.consume(builder, service, version);
            stream.append(builder.build());
        }
        System.out.println("Generated " + what + " for '" + service.getName() + "' to " + sourceFile.getAbsolutePath());
    }

    /**
     * Creates path for specified service
     *
     * @param service service to create path to
     * @return path
     */
    private String makePath(Service service) {
        String category = service.getCategory();
        if (category == null || category.length() == 0) {
            throw new IllegalArgumentException("The category must be specified for service: " + service.getName());
        } else {
            return category.replace('.', '/');
        }
    }


    private String parsePackageName(String path) {
        return DIRECTORY + path.split(DIRECTORY)[1];
    }

}
