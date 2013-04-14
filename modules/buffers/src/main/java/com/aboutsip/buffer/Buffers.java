/**
 * 
 */
package com.aboutsip.buffer;

import java.io.InputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public final class Buffers {

    private final static byte[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4',
            '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6',
            '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8',
            '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', };

    private final static byte[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };

    /**
     * All possible chars for representing a number as a String
     */
    private final static byte[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final static int[] sizeTable = {
            9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    /**
     * An empty buffer.
     */
    public static Buffer EMPTY_BUFFER = new EmptyBuffer();

    /**
     * 
     */
    private Buffers() {
        // left empty intentionally
    }

    public static Buffer wrap(final int value) {
        final int size = value < 0 ? stringSize(-value) + 1 : stringSize(value);
        final byte[] bytes = new byte[size];
        getBytes(value, size, bytes);
        return new ByteBuffer(bytes);
    }

    public static Buffer wrap(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }

        return Buffers.wrap(s.getBytes());
    }

    public static Buffer wrap(final InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("the input stream cannot be null or empty");
        }

        return new InputStreamBuffer(is);
    }

    /**
     * Create a new Buffer
     * 
     * @param capacity
     * @return
     */
    public static Buffer createBuffer(final int capacity) {
        final byte[] buffer = new byte[capacity];
        return new ByteBuffer(0, 0, buffer.length, 0, buffer);
    }

    /**
     * Wrap the supplied byte array
     * 
     * @param buffer
     * @return
     */
    public static Buffer wrap(final byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("the buffer cannot be null or empty");
        }

        return new ByteBuffer(buffer);
    }

    /**
     * Copied straight from the Integer class
     * 
     * Places characters representing the integer i into the character array
     * buf. The characters are placed into the buffer backwards starting with
     * the least significant digit at the specified index (exclusive), and
     * working backwards from there.
     * 
     * Will fail if i == Integer.MIN_VALUE
     */
    protected static void getBytes(int i, final int index, final byte[] buf) {
        int q, r;
        int charPos = index;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = i * 52429 >>> 16 + 3;
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    // Requires positive x
    protected static int stringSize(final int x) {
        for (int i = 0;; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

}
