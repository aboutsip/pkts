/**
 * 
 */
package com.aboutsip.buffer;

import java.io.InputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public final class Buffers {

    /**
     * 
     */
    private Buffers() {
        // left empty intentionally
    }

    /**
     * An empty buffer.
     */
    public static Buffer EMPTY_BUFFER = new EmptyBuffer();

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
     * Wrap the supplied byte array
     * 
     * @param buffer
     * @return
     */
    public static Buffer wrap(final byte[] buffer) {
        if ((buffer == null) || (buffer.length == 0)) {
            throw new IllegalArgumentException("the buffer cannot be null or empty");
        }

        return new ByteBuffer(buffer);
    }

}
