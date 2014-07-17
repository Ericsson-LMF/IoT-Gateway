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
package com.ericsson.deviceaccess.coap.basedriver.util;

import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.util.LinkFormatReader;
import junit.framework.TestCase;

import java.util.List;

public class LinkFormatReaderTest extends TestCase {

    public LinkFormatReaderTest() {
        super("LinkFormatReaderTest");
    }

    public void testParseLinkFormatData() {
        LinkFormatReader reader = new LinkFormatReader();

        String test1 = "</sensors/temp>;rt=\"TemperatureC\";if=\"sensor\",</sensors/light>;rt=\"LightLux\";if=\"sensor\"";

        String test2 = "<http://www.example.com/sensors/temp123>;"
                + "anchor=\"/sensors/temp\";rel=\"describedby\","
                + "</t>;anchor=\"/sensors/temp\";rel=\"alternate\"";

        try {
            List<CoAPResource> resources = reader.parseLinkFormatData(test1);
            assertEquals(resources.size(), 2);
            CoAPResource res = resources.get(0);

            assertEquals(res.getResourceType(), "\"TemperatureC\"");
            assertEquals(res.getInterfaceDescription(), "\"sensor\"");

            resources = reader.parseLinkFormatData(test2);
            res = resources.get(0);
            System.out.println("Resource name: " + res.getUri().toString());

            assertEquals(res.getUri().toString(),
                    "http://www.example.com/sensors/temp123");
            assertEquals(res.getAnchor(), "\"/sensors/temp\"");
            assertEquals(res.getRelationType(), "\"describedby\"");

            res = resources.get(1);
            assertEquals(res.getUri().toString(), "t");
            assertEquals(res.getAnchor(), "\"/sensors/temp\"");
            assertEquals(res.getRelationType(), "\"alternate\"");

            String test3 = "</sensors>;rt=\"index\";title=\"Sensor Index\",</sensors/temp>;rt=\"TemperatureC\";if=\"sensor\","
                    + "</sensors/light>;rt=\"LightLux\";if=\"sensor\",<http://www.example.com/sensors/t123>;"
                    + "anchor=\"/sensors/temp\";rel=\"describedby\",</t>;anchor=\"/sensors/temp\";rel=\"alternate\";key={\"jwk\":[{\"alg\":\"EC\",\"crv\":\"P-256\",\"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\",\"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\"use\":\"sig\",\"kid\":\"1\"},]}\"";

            resources = reader.parseLinkFormatData(test3);

            assertEquals(resources.size(), 5);
            assertEquals(resources.get(0).getUri().toString(), "sensors");
            assertEquals(resources.get(0).getResourceType(), "\"index\"");
            assertEquals(resources.get(0).getTitle(), "\"Sensor Index\"");

            assertEquals(resources.get(1).getUri().toString(), "sensors/temp");
            assertEquals(resources.get(1).getResourceType(), "\"TemperatureC\"");
            assertEquals(resources.get(1).getInterfaceDescription(),
                    "\"sensor\"");

            // Test parsing
            String test4 = "</.well-known/core>;ct=40,</careless>;rt=\"SepararateResponseTester\";"
                    + "title=\"This resource will ACK anything, but never send a separate response\","
                    + "</feedback>;rt=\"FeedbackMailSender\";title=\"POST feedback using mail\"";

            resources = reader.parseLinkFormatData(test4);

            assertEquals(resources.get(1).getUri().toString(), "careless");
            assertEquals(resources.get(1).getResourceType().toString(),
                    "\"SepararateResponseTester\"");
            assertEquals(resources.get(1).getTitle(),
                    "\"This resource will ACK anything, but never send a separate response\"");

            System.out.println("link format presentation: "
                    + res.getLinkFormatPresentation(false));

            String payload = "</sensors>;rt=\"index\";title=\"Sensor Index\",</sensors/temp>;rt=\"TemperatureC\";if=\"sensor\","
                    + "</sensors/light>;rt=\"LightLux\";if=\"sensor\",<http://www.example.com/sensors/t123>;"
                    + "anchor=\"/sensors/temp\";rel=\"describedby\",</t>;anchor=\"/sensors/temp\";rel=\"alternate\";key=\"{\"jwk\":[{\"alg\":\"EC\",\"crv\":\"P-256\",\"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\",\"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\"use\":\"sig\",\"kid\":\"1\"},]}\"";
            resources = reader.parseLinkFormatData(payload);
            assertEquals(resources.size(), 5);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
