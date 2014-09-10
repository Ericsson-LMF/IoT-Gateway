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
package com.ericsson.common.util;

import java.util.function.UnaryOperator;
import javax.xml.bind.DatatypeConverter;

/**
 * Various string utilities.
 */
public enum StringUtil {

    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Makes first char of the string to upper case
     *
     * @param string String to be capitalised
     * @return capitalised string
     */
    public static String capitalize(String string) {
        return editCharacter(string, 0, Character::toUpperCase);
    }

    public static String decapitalize(String string) {
        return editCharacter(string, 0, Character::toLowerCase);
    }

    private static String editCharacter(String string, int index, UnaryOperator<Character> editor) {
        StringBuilder sb = new StringBuilder(string);
        sb.setCharAt(index, editor.apply(sb.charAt(index)));
        return sb.toString();
    }

    public static String ensureWrapping(String prefix, String infix, String postfix) {
        if (!infix.startsWith(prefix)) {
            infix = prefix + infix;
        }
        if (!infix.endsWith(postfix)) {
            infix += postfix;
        }
        return infix;
    }

    /**
     * Ensures that there is punctuation at end of string
     *
     * @param string string to ensure punctuation
     * @return ensured string
     */
    public static String setEndPunctuation(String string) {
        return ensureWrapping("", string, ".");
    }

    /**
     * gets type from string
     *
     * @param string string to get type from
     * @return type as string
     */
    public static String getType(String string) {
        if (string.toLowerCase().startsWith("int")) {
            return "int";
        } else if (string.toLowerCase().startsWith("float")) {
            return "float";
        } else {
            return "String";
        }
    }

    public static String escapeJSON(String input) {
        StringBuilder result = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '\n') {
                result.append("\\n");
            } else if (c == '\r') {
                result.append("\\r");
            } else if (c == '"') {
                result.append("\\\"");
            } else if (c == '\\') {
                result.append("\\\\");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Encode a byte array to hex string
     *
     * @param binaryData array of byte to encode
     * @return return encoded string
     */
    public static String encode(byte[] binaryData) {
        return DatatypeConverter.printHexBinary(binaryData);
    }

    /**
     * Decode hex string to a byte array
     *
     * @param encoded encoded string
     * @return return array of byte to encode
     */
    public static byte[] decode(String encoded) {
        return DatatypeConverter.parseHexBinary(encoded);
    }

    public static boolean looseEquals(Object a, Object b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return a.toString().toLowerCase().equals(b.toString().toLowerCase());
    }

}
