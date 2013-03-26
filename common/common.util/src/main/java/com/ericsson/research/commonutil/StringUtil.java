/*
 * User: joel
 * Date: 2011-10-03
 * Time: 10:57
 *
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
