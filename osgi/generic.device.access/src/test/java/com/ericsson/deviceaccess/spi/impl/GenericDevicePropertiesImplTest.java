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

import com.ericsson.deviceaccess.api.Serializable.Format;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDPropertiesImpl;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * GenericDevicePropertiesImpl Tester.
 *
 */
public class GenericDevicePropertiesImplTest {

    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private GDPropertyMetadata metadataFloat;
    private List<GDPropertyMetadata> metadataArr;
    private GDPropertiesImpl props;

    @Before
    public void setUp() throws Exception {
        metadataFloat = context.mock(GDPropertyMetadata.class, "metadataFloat");
        metadataArr = new ArrayList<>();
        metadataArr.add(metadataFloat);

        context.checking(new Expectations() {
            {
                allowing(metadataFloat).getDefaultNumberValue();
                will(returnValue(42.0f));
                allowing(metadataFloat).getDefaultStringValue();
                will(returnValue("42.0"));
                allowing(metadataFloat).getName();
                will(returnValue("fProp"));
                allowing(metadataFloat).getType();
                will(returnValue(Float.class));
                allowing(metadataFloat).getTypeName();
                will(returnValue("Float"));
                allowing(metadataFloat).getValidValues();
                will(returnValue(new String[0]));
                allowing(metadataFloat).getMinValue();
                will(returnValue(Float.NEGATIVE_INFINITY));
                allowing(metadataFloat).getMaxValue();
                will(returnValue(Float.POSITIVE_INFINITY));
                allowing(metadataFloat).serialize(Format.JSON);
                will(returnValue("{\"type\":\"float\"}"));
            }
        });

        props = new GDPropertiesImpl(metadataArr, null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSerialize() throws GDException {
        context.checking(new Expectations() {
            {
            }
        });
        String json = props.serialize(Format.JSON);
        System.out.println(json);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (JSONException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSerializeState() {
        String json = props.serializeState();
        System.out.println(json);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (JSONException e) {
            Assert.fail(e.getMessage());
        }
    }
}
