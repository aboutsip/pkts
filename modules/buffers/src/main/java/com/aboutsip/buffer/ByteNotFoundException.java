/**
 * 
 */
package com.aboutsip.buffer;

/**
 * Exception for readUntil-methods and the like
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public class ByteNotFoundException extends BufferException {

    private final byte b;

    /**
     * 
     * @param b
     */
    public ByteNotFoundException(final byte b) {
        // TODO: convert to hex string as well
        super("Unable to locate byte " + b);
        this.b = b;
    }

    /**
     * The byte that the user search for but we couldn't find.
     * 
     * @return
     */
    public byte getByte() {
        return this.b;
    }

}
