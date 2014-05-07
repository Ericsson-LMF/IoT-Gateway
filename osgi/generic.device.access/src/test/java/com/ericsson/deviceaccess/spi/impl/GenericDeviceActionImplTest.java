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

package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;

import static junit.framework.Assert.fail;

/**
 * 
 */
public class GenericDeviceActionImplTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private GenericDevicePropertyMetadata floatPropMetadata;
    private GenericDevicePropertyMetadata stringPropMetadata;
    private GenericDevicePropertyMetadata intPropMetadata;
    private GenericDeviceActionImpl action;

    @Before
    public void setup() throws GenericDeviceException {
        floatPropMetadata = context.mock(GenericDevicePropertyMetadata.class, "floatPropMetadata");
        intPropMetadata = context.mock(GenericDevicePropertyMetadata.class, "intPropMetadata");
        stringPropMetadata = context.mock(GenericDevicePropertyMetadata.class, "stringPropMetadata");
        GenericDevicePropertyMetadata[] resultMetadata = new GenericDevicePropertyMetadata[] {
                floatPropMetadata, 
                intPropMetadata, 
                stringPropMetadata, 
            };
        GenericDevicePropertyMetadata[] argumentsMetadata = new GenericDevicePropertyMetadata[] { 
                floatPropMetadata, 
                intPropMetadata, 
                stringPropMetadata, 
            };
        context.checking(new Expectations() {{
            allowing(intPropMetadata).getName();will(returnValue("int"));
            allowing(intPropMetadata).getSerializedNode("",GenericDevice.FORMAT_JSON);will(returnValue("{\"type\":\"int\"}"));
            allowing(floatPropMetadata).getName();will(returnValue("float"));
            allowing(floatPropMetadata).getSerializedNode("",GenericDevice.FORMAT_JSON);will(returnValue("{\"type\":\"float\"}"));
            allowing(stringPropMetadata).getName();will(returnValue("string"));
            allowing(stringPropMetadata).getSerializedNode("",GenericDevice.FORMAT_JSON);will(returnValue("{\"type\":\"string\"}"));

        }});
    
        action = new GenericDeviceActionImpl("action", argumentsMetadata, resultMetadata);
    }
    
    @Test
    public void testSerialize() throws GenericDeviceException, JSONException {
        String json = action.serialize(GenericDevice.FORMAT_JSON);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
