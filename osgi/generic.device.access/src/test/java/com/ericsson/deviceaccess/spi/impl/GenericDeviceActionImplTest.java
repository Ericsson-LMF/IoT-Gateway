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

import com.ericsson.common.util.serialization.Format;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDActionImpl;
import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.api.ExpectationError;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GenericDeviceActionImplTest {

    private JUnit4Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private GDPropertyMetadata floatPropMetadata;
    private GDPropertyMetadata stringPropMetadata;
    private GDPropertyMetadata intPropMetadata;
    private GDActionImpl action;

    @Before
    public void setup() throws GDException {
        floatPropMetadata = context.mock(GDPropertyMetadata.class, "floatPropMetadata");
        intPropMetadata = context.mock(GDPropertyMetadata.class, "intPropMetadata");
        stringPropMetadata = context.mock(GDPropertyMetadata.class, "stringPropMetadata");
        List<GDPropertyMetadata> resultMetadata = new ArrayList<>();
        resultMetadata.add(floatPropMetadata);
        resultMetadata.add(intPropMetadata);
        resultMetadata.add(stringPropMetadata);
        List<GDPropertyMetadata> argumentsMetadata = new ArrayList<>();
        argumentsMetadata.add(floatPropMetadata);
        argumentsMetadata.add(intPropMetadata);
        argumentsMetadata.add(stringPropMetadata);
        context.checking(new Expectations() {
            {
                allowing(intPropMetadata).getName();
                will(returnValue("int"));
                allowing(intPropMetadata).getDefaultStringValue();
                will(returnValue(""));
                allowing(intPropMetadata).getMaxValue();
                will(returnValue(null));
                allowing(intPropMetadata).getMinValue();
                will(returnValue(null));
                allowing(intPropMetadata).getTypeName();
                will(returnValue("Integer"));
                allowing(intPropMetadata).getValidValues();
                will(returnValue(null));
                allowing(intPropMetadata).getSerializedNode("", Format.JSON);
                will(returnValue("{\"type\":\"int\"}"));

                allowing(floatPropMetadata).getName();
                will(returnValue("float"));
                allowing(floatPropMetadata).getDefaultStringValue();
                will(returnValue(""));
                allowing(floatPropMetadata).getMaxValue();
                will(returnValue(null));
                allowing(floatPropMetadata).getMinValue();
                will(returnValue(null));
                allowing(floatPropMetadata).getTypeName();
                will(returnValue("Float"));
                allowing(floatPropMetadata).getValidValues();
                will(returnValue(null));
                allowing(floatPropMetadata).getSerializedNode("", Format.JSON);
                will(returnValue("{\"type\":\"float\"}"));

                allowing(stringPropMetadata).getName();
                will(returnValue("string"));
                allowing(stringPropMetadata).getSerializedNode("", Format.JSON);
                will(returnValue("{\"type\":\"string\"}"));
                allowing(stringPropMetadata).getDefaultStringValue();
                will(returnValue(""));
                allowing(stringPropMetadata).getMaxValue();
                will(returnValue(null));
                allowing(stringPropMetadata).getMinValue();
                will(returnValue(null));
                allowing(stringPropMetadata).getTypeName();
                will(returnValue("String"));
                allowing(stringPropMetadata).getValidValues();
                will(returnValue(null));
            }
        });

        action = new GDActionImpl("action", argumentsMetadata, resultMetadata);
    }

    @Test
    public void testSerialize() throws GDException, JSONException {
        String json = null;
        try {
            json = action.serialize(Format.JSON);
        } catch (ExpectationError ex) {
            System.out.println(ex.invocation);
        }

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

}
