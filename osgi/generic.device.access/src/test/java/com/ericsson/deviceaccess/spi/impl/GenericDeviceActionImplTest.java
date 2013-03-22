/*
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
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
