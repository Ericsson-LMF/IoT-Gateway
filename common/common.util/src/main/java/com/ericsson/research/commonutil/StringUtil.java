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
package com.ericsson.research.commonutil;

/**
 * Various string utilities that are not supported in CDC.
 */
public class StringUtil {
    private StringUtil() {
    }

    /**
     * Escapes all '&' characters with '&amp;' in the specified string.
     *
     * @param string the input string
     *
     * @return the string with the escaped values.
     */
    public static String escapeAmpersand(String string) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '&') {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Creates string of array.
     * @param objectArray
     * @return
     */
    public static String toString(Object[] objectArray) {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < objectArray.length; i++) {
            sb.append(objectArray[i]);
            if (i < objectArray.length - 1) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toHexString(byte byteValue) {
        return Integer.toHexString(((int)byteValue) & 0xFF);
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < bytes.length; i++) {
            byte byteValue = bytes[i];
            sb.append(' ').append(Integer.toHexString(byteValue & 0xFF));
        }
        sb.append(" ]");
        return sb.toString();
    }

    public static String toHexString(int[] ints) {
        if (ints == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < ints.length; i++) {
            int intValue = ints[i];
            sb.append(' ').append(Integer.toHexString(intValue));
        }
        sb.append(" ]");
        return sb.toString();
    }

    public static String toString(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]).append(',');
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(']');
        return sb.toString();
    }

    public static String toString(int[] arr) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]).append(',');
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(']');
        return sb.toString();
    }

}
