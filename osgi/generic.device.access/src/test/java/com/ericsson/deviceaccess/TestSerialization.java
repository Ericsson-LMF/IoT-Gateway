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
package com.ericsson.deviceaccess;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceActivator;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceActionImpl;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceServiceImpl;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import java.util.Dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSerialization {

    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    // String template = "{\"action\":{\"test\":{\"arguments\":{\"requester\":null},\"name\":\"test\"}},\"name\":\"test\",\"parameter\":{\"math\":{\"name\":\"math\",\"value\":\"100\"}},\"status\":null}";
    String template = "{\"name\":\"test\",\"actions\":[{\"name\":\"action\",\"arguments\": [{\"name\":\"arg\",\"type\":\"java.lang.Integer\",\"minValue\":\"-10\",\"maxValue\":\"10\",\"defaultValue\":\"0\"},{\"name\":\"arg2\",\"type\":\"java.lang.Integer\",\"minValue\":\"-10\",\"maxValue\":\"10\",\"defaultValue\":\"0\"}],\"result\": [{\"name\":\"res1\",\"type\":\"java.lang.Integer\",\"minValue\":\"-2147483648\",\"maxValue\":\"2147483647\",\"defaultValue\":\"0\"}]}],\"properties\":[{\"prop1\":\"100\"}]}";
    ServiceSchema serviceSchema = new ServiceSchema.Builder("@@TEST@@").
            addActionSchema(new ActionSchema.Builder("action").
                    setMandatory(true).
                    addArgumentSchema(new ParameterSchema.Builder("arg", Integer.class).
                            setMinValue("-10").
                            setMaxValue("10").
                            build()).
                    addArgumentSchema(new ParameterSchema.Builder("arg2", Integer.class).
                            setMinValue("-10").
                            setMaxValue("10").
                            build()).
                    addResultSchema(new ParameterSchema.Builder("res1", Integer.class).
                            build()).
                    build()).
            addActionSchema(new ActionSchema.Builder("optionalAction").
                    build()).
            addPropertySchema(new ParameterSchema.Builder("prop1", Integer.class).
                    build()).
            build();

    /*
     * @Test public void serializeTestServiceWithJSONIC(){ TestService test =
     * new TestService(); GenericDeviceActionImpl act = new
     * GenericDeviceActionImpl(); act.setName("test"); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); String json =
     * JSON.encode(test); System.out.println(json);
     * 
     * TestService test2 = JSON.decode(json, TestService.class);
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName()); }
     */

    /*
     * @Test public void serializeTestDeviceWithJSONIC(){ GenericDeviceImpl dev
     * = new GenericDeviceImpl(); TestService test = new TestService();
     * dev.putService(test); GenericDeviceActionImpl act = new
     * GenericDeviceActionImpl(); act.setName("test"); GenericDeviceProperties
     * args = new GenericDevicePropertiesImpl(); args.setStringValue("testArg",
     * "testArgValue"); act.setArguments(args); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); String json =
     * JSON.encode(dev); System.out.println(json);
     * 
     * GenericDeviceImpl dev2 = JSON.decode(json, GenericDeviceImpl.class);
     * GenericDeviceService test2 = dev2.getService(test.getName());
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName());
     * assertEquals(test2.getAction("test").getArguments
     * ().getStringValue("testArg"),
     * test.getAction("test").getArguments().getStringValue("testArg")); }
     */
//    @Test
//    public void testInterfaceUnmarshal() {
//        GenericDeviceAction action = JSON.decode(template, GenericDeviceActionImpl.class);
//    }
    @Test
    public void testGetLeafNode() throws Exception {
        GenericDeviceImpl dev = new GenericDeviceImpl() {
        };
        TestService test = new TestService();
        test.getProperties().setIntValue("prop1", 10);
        dev.putService(test);
        String node = dev.getSerializedNode("service/test/parameter/prop1", Serializable.FORMAT_JSON);

        assertEquals("10", node);
    }

    @Test
    public void testGetServiceNode() throws Exception {
        GenericDeviceImpl dev = new GenericDeviceImpl() {
        };
        TestService test = new TestService();
        test.getProperties().setIntValue("prop1", 100);
        dev.putService(test);
        String node = dev.getSerializedNode("service/test", Serializable.FORMAT_JSON);
        System.out.println(node);

        assertTrue(node.indexOf("prop1") > 0);

        node = dev.getSerializedNode("service/test/parameter", Serializable.FORMAT_JSON);
        System.out.println(node);

        assertTrue(node.indexOf("prop1") > 0);

        node = dev.getSerializedNode("service/test/action", Serializable.FORMAT_JSON);
        System.out.println(node);
        assertTrue(node.indexOf("arg2") > 0);

        node = dev.getSerializedNode("service/test/action/action", Serializable.FORMAT_JSON);
        System.out.println(node);

        assertTrue(node.indexOf("arg2") > 0);
        assertTrue(node.indexOf("res1") > 0);
    }

    @Test
    public void testGetNonExistingNode() throws Exception {
        GenericDeviceImpl dev = new GenericDeviceImpl() {
        };
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {
            {
                oneOf(eventManager).addEvent(with(aNonNull(String.class)), with(aNonNull(String.class)), with(aNonNull(Dictionary.class)));
            }
        });

        dev.setName("dev");
        Object node = null;
        try {
            node = dev.getSerializedNode("service/nonexist", Serializable.FORMAT_JSON);
        } catch (GenericDeviceException e) {

        }

        assert (node == null);
    }

    /*
     * @Test public void testServiceToJson() throws Exception{ TestService test
     * = new TestService(); GenericDeviceActionImpl act = new
     * GenericDeviceActionImpl(); act.setName("test"); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); String json =
     * test.serialize(com.ericsson.deviceaccess.api.Constants.FORMAT_JSON);
     * System.out.println(json);
     * 
     * TestService test2 = JSON.decode(json, TestService.class);
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName()); }
     */

    /*
     * @Test public void testDeviceToJson() throws Exception { GenericDevice dev
     * = new GenericDeviceImpl(); TestService test = new TestService();
     * GenericDeviceActionImpl act = new GenericDeviceActionImpl();
     * act.setName("test"); test.putAction(act);
     * test.getParameter().setIntValue("math", 100); dev.putService(test);
     * String json =
     * dev.serialize(com.ericsson.deviceaccess.api.Constants.FORMAT_JSON);
     * System.out.println(json);
     * 
     * GenericDevice dev2 = JSON.decode(json, GenericDeviceImpl.class);
     * GenericDeviceService test2 = dev2.getService("test");
     * assertEquals(test2.getName(), test.getName());
     * assertEquals(test2.getParameter().getStringValue("math"),
     * test.getParameter().getStringValue("math"));
     * assert(test2.getAction("test") != null);
     * assertEquals(test2.getAction("test").getName(),
     * test.getAction("test").getName()); }
     */

    /*
     * @Test public void testDecodeActionContext(){ String encoded =
     * "{\"action\":\"play\",\"arguments\":{\"requester\":\"kenta\",\"url\":\"\"},\"authorized\":true,\"device\":\"5855CA21A8B7\",\"executed\":false,\"failed\":false,\"firstTime\":true,\"messageThreadId\":24451742,\"owner\":\"kenta\",\"requester\":\"kenta\",\"requesterContact\":\"warp://erlabs:gateway/731/1314098276490/context/kenta/device/5855CA21A8B7/service/renderingControl/action/play\",\"result\":{\"code\":0,\"reason\":null,\"value\":{}},\"service\":\"renderingControl\"}"
     * ; GenericDeviceActionContextImpl cont = JSON.decode(encoded,
     * GenericDeviceActionContextImpl.class); }
     */
    class TestService extends GenericDeviceServiceImpl {

        public TestService() {
            super("test", serviceSchema.getPropertiesSchemas());
            init();
        }

        private void init() {
            putAction(new TestAction());
        }

        class TestAction extends GenericDeviceActionImpl {

            public TestAction() {
                super("action", serviceSchema.getActionSchemas()[0].getArgumentsSchemas(), serviceSchema.getActionSchemas()[0].getResultSchema());
            }
        }
    }
}
