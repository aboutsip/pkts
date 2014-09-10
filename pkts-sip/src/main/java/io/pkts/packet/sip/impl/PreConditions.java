/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;

/**
 * Contains common checks for null etc. All assertXXXX will throw a SipParseException. All checkXXX will
 * return a boolean.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class PreConditions {

    private PreConditions() {}

    public static <T> T assertNotNull(final T reference, final String msg) throws SipParseException {
        if (reference == null) {
            throw new SipParseException(msg);
        }
        return reference;
    }

    public static <T> T assertNotNull(final T reference) throws SipParseException {
        if (reference == null) {
            throw new SipParseException("Value cannot be null");
        }
        return reference;
    }

    /**
     * Check if a string is empty, which includes null check.
     * 
     * @param string
     * @return true if the string is either null or empty
     */
    public static boolean checkIfEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean checkIfNotEmpty(final String string) {
        return !checkIfEmpty(string);
    }

    public static String assertNotEmpty(final String reference, final String msg) throws SipParseException {
        if (reference == null || reference.isEmpty()) {
            throw new SipParseException(msg);
        }
        return reference;
    }

    public static void assertArgument(final boolean expression, final String msg) throws SipParseException {
        if (!expression) {
            throw new SipParseException(msg);
        }
    }

    /**
     * Assert that the {@link Buffer} is not null nor empty.
     * 
     * @param reference
     * @param msg
     * @return
     * @throws IllegalArgumentException
     */
    public static Buffer assertNotEmpty(final Buffer reference, final String msg) throws SipParseException {
        if (reference == null || reference.isEmpty()) {
            throw new SipParseException(msg);
        }
        return reference;
    }


    /**
     * If our reference is null then return a default value instead.
     * 
     * @param reference the thing to check.
     * @param defaultValue the default value to return if the above reference is null.
     * @return the reference if not null, otherwise the default value. Note, if your default value
     *         is null as well then you will get back null, since that is what you asked. Chain with
     *         {@link #assertNotNull(Object, String)} if you want to make sure you have a non-null
     *         value for the default value.
     */
    public static <T> T ifNull(final T reference, final T defaultValue) {
        if (reference == null) {
            return defaultValue;
        }
        return reference;
    }

}
