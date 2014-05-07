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

package com.ericsson.deviceaccess.spi.schema.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchemaError;

/**
 * 
 */
public class ParameterSchemaTest {

    @Test
    public void testNumber() {
        ParameterSchema parameterSchema = new ParameterSchema.Builder("par1").
                setType(Integer.class).
                setDefaultValue(new Integer(21)).
                setMaxValue("99").
                setMinValue("4").
                build();

        assertEquals("par1", parameterSchema.getName());
        assertSame(Integer.class, parameterSchema.getType());
        assertEquals("21", parameterSchema.getDefaultStringValue());
        assertEquals(21, parameterSchema.getDefaultNumberValue().intValue());
        assertEquals(99, parameterSchema.getMaxValue().intValue());
        assertEquals(4, parameterSchema.getMinValue().intValue());
    }

    @Test
    public void testString() {
        ParameterSchema parameterSchema = new ParameterSchema.Builder("par1").
                setType(String.class).
                setDefaultValue("banan").
                setValidValues(new String[] { "orange", "banan" }).
                build();

        assertEquals("par1", parameterSchema.getName());
        assertSame(String.class, parameterSchema.getType());
        assertEquals("banan", parameterSchema.getDefaultStringValue());
        assertArrayEquals(new String[] { "banan", "orange" }, parameterSchema.getValidValues());
    }

    @Test
    public void testWrongDefault() {
        try {
            ParameterSchema parameterSchema = new ParameterSchema.Builder("par1").
                    setType(String.class).
                    setDefaultValue(new Integer(2)).
                    build();
            fail("should cause exception");
        } catch (ServiceSchemaError e) {
            // success
        }
    }
}
