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
package com.ericsson.deviceaccess.tutorial;

import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.deviceaccess.spi.schema.based.SBServiceBase;

/**
 * Example of a custom service which is not defined in the service.xml, and for
 * which there exists no base class.
 */
public class CustomService extends SBServiceBase {

    public static final String SERVICE_NAME = "MyCustomService";

    /**
     * This is the schema for the action "MyCustomAction", which accepts the
     * following arguments:
     * <ul>
     * <li>arg1:String</li>
     * <li>arg2:Int with default value: 20
     * </ul>
     * and returns the following results
     * <ul>
     * <li>res1:Float with default value: 0.0</li>
     * <li>res2:String with valid values: "On" and "Off"</li>
     * </ul>
     */
    private final static ActionSchema MY_ACTION = new ActionSchema.Builder("MyCustomAction").
            addArgument("arg1", String.class).
            addArgument(p -> {
                p.setName("arg2");
                p.setType(Integer.class);
                p.setDefault("20");
                p.setMinValue("10");
                p.setMaxValue("45");
            })
            .addResult("res1", Float.class)
            .addResult(p -> {
                p.setName("res2");
                p.setType(String.class);
                p.setDefault("On");
                p.setValidValues("On", "Off");
            })
            .build();

    /**
     * This is the schema for this custom service. Define a schema
     * "MyCustomService" with:
     * <ul>
     * <li>one action "MyCustomAction"</li>
     * <li>and one property (state): "prop1", which is a String with valid
     * values: "Active" and "Inactive"</li>
     * </ul>
     */
    private static ServiceSchema SERVICE_SCHEMA = new ServiceSchema.Builder(SERVICE_NAME)
            .addAction(MY_ACTION)
            .addProperty(p -> {
                p.setName("prop1");
                p.setType(String.class);
                p.setDefault("Active");
                p.setValidValues("Active", "Inactive");
            })
            .build();

    /**
     * Create an instance.
     */
    public CustomService() {
        super(SERVICE_SCHEMA);

        // Define the action
        defineAction("MyCustomAction", context -> {
            String arg1 = context.getArguments().getStringValue("arg1");
            int arg2 = context.getArguments().getIntValue("arg2");
            context.getResult().getValue().setFloatValue("res1", arg2 + arg1.length());
            context.getResult().getValue().setStringValue("res2", arg2 + arg1.length() > 30 ? "On" : "Off");
        });
    }

    /**
     * This method is called by the base driver which simulates updates when the
     * active state of the custom device.
     * <p/>
     * It updates the <i>prop1</i> property using the
     * <i>getProperties().setStringValue(...)</i>
     * method provided by the base class.
     *
     * @param active
     */
    public void update(boolean active) {
        getProperties().setStringValue("prop1", active ? "Active" : "Inactive");
    }
}
