
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

/**
 * A class that contains methods for manipulating binary values
 *
 */
public enum BitUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    public static final boolean ONE = true;
    public static final boolean ZERO = false;

    /**
     * Get value of bit n in an integer (32 bits)
     *
     * @param i is a 32-bit value
     * @param n is the bit whose values is to be returned
     */
    public static byte getBitInInt(int i, int n) {
        return (byte) ((i >> n) & 1);
    }

    /**
     * Get value of bit n in a long (64 bits)
     *
     * @param l is a 64-bit value
     * @param n is the bit whose values is to be returned
     */
    public static byte getBitInLong(long l, int n) {
        return (byte) ((l >> (long) n) & 1L);
    }

    /**
     * Get value of bit n in a byte (8 bits)
     *
     * @param i is an 8-bit value
     * @param n is the bit whose value is to be returned
     */
    public static byte getBitInByte(byte i, int n) {
        return (byte) ((i >> n) & 1);
    }

    /**
     * Return value with bit n of an 8-bit sequence set to v (0 or 1)
     *
     * @param b is an 8-bit value
     * @param n is the bit that should be set
     * @param v is the value that should be set (0 or 1)
     * @return
     */
    public static byte setBitInByte(byte b, int n, int v) {
        int temp = 1 << n;
        if (v != 0) {
            return (byte) (b | temp); // turn on the bit
        } else {
            return (byte) (b & ~(temp)); // turn off the bit
        }
    }

    /**
     * Set bit n of a 32-bit sequence i to v (0 or 1)
     *
     * @param i is a 32-bit value
     * @param n is the bit that should be set
     * @param v is the value (0 or 1)
     * @return
     */
    public static int setBitInInt(int i, int n, int v) {
        if (v != 0) {
            return i | (1 << n); // turn on the bit
        } else {
            return i & ~(1 << n); // turn off the bit
        }
    }

    /**
     * Set bit n of a 32-bit sequence i to v (0 or 1)
     *
     * @param i is a 32-bit value
     * @param n is the bit that should be set
     * @param v is a boolean value
     * @return
     */
    public static int setBitInInt(int i, int n, boolean v) {
        if (v == ONE) {
            return i | (1 << n); // turn on the bit
        } else {
            return i & ~(1 << n); // turn off the bit
        }
    }

    /**
     * Set bit n of a 64-bit sequence i to v (0 or 1)
     *
     * @param i is a 64-bit value
     * @param n is the bit that should be set
     * @param v is a boolean value
     * @return
     */
    public static long setBitInLong(long l, long n, int v) {
        if (v != 0) {
            return l | (1L << n); // turn on the bit
        } else {
            return l & ~(1L << n); // turn off the bit
        }
    }

    /**
     * Set k bits starting from bit n in a 32-bit sequence according to v, i.e.,
     * bits from n to n+k of 'i' are set according to 'v'. So if v=1010 and k=4,
     * bits from n to n+4 of 'i' are set to 1010.
     *
     * @param i is a 32-bit sequence
     * @param n is the starting position
     * @param k is the number of bits to be set
     * @param v contains the value
     * @return
     */
    public static int setBitsInInt(int i, int n, int k, int v) {
        return (i & ~(~(~0 << k) << n)) | (v << n);
    }

    /**
     * Return a 'long' value with k bits starting at bit n assigned according to
     * v I.e. bits from n to n+k of 'i' are set according to 'v'. So if v=1010
     * and k=4, bits from n to n+4 of 'i' are set to 1010. Note that the
     * argument 'v' must be a long, since it is shifted by 'n', the maximum
     * value for which is 63.
     *
     * @param l is the long value to be manipulated
     * @param n is the starting position
     * @param k is the number of bits to set
     * @param v is the value according to which the bits are set
     * @return the resulting long value
     */
    public static long setBitsInLong(long l, int n, int k, long v) {
        long temp = ~(~0L << k);
        long temp2 = ~(temp << n);
        long temp3 = l & temp2;
        return temp3 | (v << n);
    }

    /**
     * Set k bits starting from bit n in the value i to v. I.e. bits from n to
     * n+k of 'i' are set according to 'v'. So if v=1010 and k=4, bits from n to
     * n+4 of 'i' are set to 1010.
     *
     * @param i is the value in which bits should be set
     * @param n is the starting position
     * @param k is the number of bits to set
     * @param v is the value according to which the bits are set
     * @return an 8-bit value
     */
    public static byte setBitsInByte(int i, int n, int k, int v) {
        return (byte) ((i & ~(~(~0 << k) << n)) | (v << n));
    }

    /**
     * Set k bits starting from bit n in value s according to v. I.e., bits from
     * n to n+k of 'i' are set according to 'v'. So if v=1010 and k=4, bits from
     * n to n+4 of 'i' are set to 1010.
     *
     * @param s is the value in which bits should be set
     * @param n is the starting position
     * @param k is the number of bits to set
     * @param v is the value according to which the bits are set
     * @return a 16-bit value
     */
    public static short setBitsInShort(short s, int n, int k, int v) {
        return (short) ((s & ~(~(~0 << k) << n)) | (v << n));
    }

    /**
     * Get the value of k bits starting from position n in an integer i.
     *
     * @param i is an integer value
     * @param n is the starting position
     * @param k is the number of bits to bet
     * @return a byte value
     */
    public static byte getBitsInIntAsByte(int i, int n, int k) {
        return (byte) ((i & (~(~0 << k) << n)) >> n);
    }

    /**
     * Get the value of k bits at position n in an integer i
     *
     * @param i is an integer value
     * @param n is the starting position
     * @param k is the number of bits to get
     * @return a byte value
     */
    public static short getBitsInIntAsShort(int i, int n, int k) {
        return (short) ((i & (~(~0 << k) << n)) >> n);
    }

    /**
     * Get value of k bits at position n in a long. Return a byte.
     *
     * @param l is the long value
     * @param n is the position from which the bit sequence to get starts
     * @param k is the number of bits to get.
     * @return a bit sequence of k bits
     */
    public static int getBitsInLongAsInt(long l, int n, int k) {
        // When a long value is shifted (i.e. the left-hand operand
        // is a long), only the last 6 digits of the right-hand operand
        // are used to perform the shift. For an int, only the five
        // last digits are used. Thus, the maximum number of positions
        // to shift is 32 for an int operand. Therefore, make sure
        // that when shifting a long, there are no temporary int
        // values.
        long temp = ~(~0L << (long) k);
        long temp2 = temp << (long) n;
        long temp3 = l & temp2;
        return (int) (temp3 >> n);
    }

    /**
     * Get value of k bits at position n in a long. Return a byte.
     *
     * @param l is the long value
     * @param n is the position from which the bit sequence to get starts
     * @param k is the number of bits to get.
     * @return a bit sequence of k bits
     */
    public static byte getBitsInLongAsByte(long l, int n, int k) {
        // When a long value is shifted (i.e. the left-hand operand
        // is a long), only the last 6 digits of the right-hand operand
        // are used to perform the shift. For an int, only the five
        // last digits are used. Thus, the maximum number of positions
        // to shift is 32 for an int operand. Therefore, make sure
        // that when shifting a long, there are no temporary int
        // values.
        long temp = ~(~0L << k);
        long temp2 = temp << n;
        long temp3 = l & temp2;
        return (byte) (temp3 >> n);
    }

    /**
     * Get value of k bits at position n in an integer. Return a short.
     *
     * @param i
     * @param n
     * @param k
     * @return
     */
    public static short getBitsInLongAsShort(long i, int n, int k) {
        return (short) ((i & (~(~0 << k) << n)) >> n);
    }

    /**
     * Get value of k bits at position n in byte i. Return a byte.
     *
     * @param i
     * @param n
     * @param k
     * @return
     */
    public static byte getBitsInByteAsByte(byte i, int n, int k) {
        return (byte) ((i & (~(~0 << k) << n)) >> n);
    }

    /**
     * Get value of k bits at position n in a short. Return a byte.
     *
     * @param i
     * @param n
     * @param k
     * @return
     */
    public static byte getBitsInShortAsByte(short i, int n, int k) {
        return (byte) ((i & (~(~0 << k) << n)) >> n);
    }

    /**
     * Get value of k bits at position n in an integer. Return an integer.
     *
     * @param i
     * @param n
     * @param k
     * @return
     */
    public static int getBitsInIntAsInt(int i, int n, int k) {
        return (i & (~(~0 << k) << n)) >> n;
    }

    /**
     * Splits an integer (32 bits) into four bytes (8 bits each)
     *
     * @param i
     * @return
     */
    public static byte[] splitIntToBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[3] = getBitsInIntAsByte(i, 0, 8);
        bytes[2] = getBitsInIntAsByte(i, 8, 8);
        bytes[1] = getBitsInIntAsByte(i, 16, 8);
        bytes[0] = getBitsInIntAsByte(i, 24, 8);
        return bytes;
    }

    /**
     * Splits a long (64 bits) into eight bytes (8 bits each)
     *
     * @param l
     * @return
     */
    public static byte[] splitLongToBytes(long l) {
        byte[] bytes = new byte[8];
        bytes[7] = getBitsInLongAsByte(l, 0, 8);
        bytes[6] = getBitsInLongAsByte(l, 8, 8);
        bytes[5] = getBitsInLongAsByte(l, 16, 8);
        bytes[4] = getBitsInLongAsByte(l, 24, 8);
        bytes[3] = getBitsInLongAsByte(l, 32, 8);
        bytes[2] = getBitsInLongAsByte(l, 40, 8);
        bytes[1] = getBitsInLongAsByte(l, 48, 8);
        bytes[0] = getBitsInLongAsByte(l, 56, 8);
        return bytes;
    }

    /**
     * Splits a short (16 bits) into two bytes (8 bits each)
     *
     * @param i
     * @return
     */
    public static byte[] splitShortToBytes(short i) {
        byte[] bytes = new byte[2];
        bytes[1] = getBitsInShortAsByte(i, 0, 8);
        bytes[0] = getBitsInShortAsByte(i, 8, 8);
        return bytes;
    }

    /**
     * Merge eight bytes into a long
     *
     * @param a is the first byte
     * @param b is the second byte
     * @param c is the third byte
     * @param d is the fourth byte
     * @param e is the fifth byte
     * @param f is the sixth byte
     * @param g is the seventh byte
     * @param h is the eight byte
     * @return
     */
    public static long mergeBytesToLong(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h) {
        long result = 0;
        result = setBitsInLong(result, 0, 8, h);
        result = setBitsInLong(result, 8, 8, g);
        result = setBitsInLong(result, 16, 8, f);
        result = setBitsInLong(result, 24, 8, e);
        result = setBitsInLong(result, 32, 8, d);
        result = setBitsInLong(result, 40, 8, c);
        result = setBitsInLong(result, 48, 8, b);
        result = setBitsInLong(result, 56, 8, a);
        return result;
    }

    /**
     * Merge four bytes in to an integer
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    public static int mergeBytesToInt(byte a, byte b, byte c, byte d) {
        int result = 0;
        result = setBitsInInt(result, 0, 8, d);
        result = setBitsInInt(result, 8, 8, c);
        result = setBitsInInt(result, 16, 8, b);
        result = setBitsInInt(result, 24, 8, a);
        return result;
    }

    /**
     * Merge two bytes into a short
     *
     * @param a
     * @param b
     * @return
     */
    public static short mergeBytesToShort(byte a, byte b) {
        short result = 0;
        result = setBitsInShort(result, 0, 8, b);
        result = setBitsInShort(result, 8, 8, a);
        return result;
    }

    /**
     * Get a string presentation of the 64 bits in a long
     *
     * @param integer
     * @return
     */
    public static String longToString(long longV) {
        StringBuilder result = new StringBuilder("{");
        int counter = 0;
        for (int ind = 63; ind >= 0; ind--) {
            if (getBitInLong(longV, ind) == 1) {
                result.append("1");
                counter++;
            } else {
                result.append("0");
                counter++;
            }
            if (counter % 8 == 0 && counter != 64) {
                result.append("|");
            }
        }
        result.append("}");
        return result.toString();

    }

    /**
     * Get a string presentation of the 32 bits in the integer
     *
     * @param integer
     * @return
     */
    public static String intToString(int integer) {
        StringBuilder result = new StringBuilder("{");
        int counter = 0;
        for (int ind = 31; ind >= 0; ind--) {
            if (getBitInInt(integer, ind) == 1) {
                result.append("1");
                counter++;
            } else {
                result.append("0");
                counter++;
            }
            if (counter % 8 == 0 && counter != 32) {
                result.append("|");
            }
        }
        result.append("}");
        return result.toString();

    }

    /**
     * Get a string presentation of the 8 bits in a byte
     *
     * @param b
     * @return
     */
    public static String byteToString(int b) {
        StringBuilder result = new StringBuilder("{");
        for (int ind = 7; ind >= 0; ind--) {
            if (getBitInInt(b, ind) == 1) {
                result.append("1");
            } else {
                result.append("0");
            }
        }
        result.append("}");
        return result.toString();
    }

    /**
     * Convert short to unsigned int.
     *
     * @param bytes
     * @return unsigned int representation of the short or -1 if invalid
     */
    public static int shortToUnsignedInt(byte[] bytes) {
        short shortInt = -1;
        switch (bytes.length) {
            case 1:
                shortInt = BitUtil.mergeBytesToShort((byte) 0, bytes[0]);
                break;
            case 2:
                shortInt = BitUtil.mergeBytesToShort(bytes[0], bytes[1]);
                break;
        }
        return shortInt & 0xFFFF;
    }

    /**
     *
     * Convert integer (max 4 bytes) to unsigned long.
     *
     * @param bytes
     * @return unsigned long representation of the int or -1 if invalid
     */
    public static long convertIntToUnsignedLong(byte[] bytes) {
        int intValue = -1;
        byte zeroByte = 0;
        switch (bytes.length) {
            case 1:
                intValue = BitUtil.mergeBytesToInt(zeroByte, zeroByte, zeroByte, bytes[0]);
                break;
            case 2:
                intValue = BitUtil.mergeBytesToInt(zeroByte, zeroByte, bytes[0], bytes[1]);
                break;
            case 3:
                intValue = BitUtil.mergeBytesToInt(zeroByte, bytes[0], bytes[1], bytes[2]);
                break;
            case 4:
                intValue = BitUtil.mergeBytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
                break;
        }
        return intValue & 0xffffffffL;
    }
}
