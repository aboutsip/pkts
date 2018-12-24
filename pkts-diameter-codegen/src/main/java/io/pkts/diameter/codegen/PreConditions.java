/**
 *
 */
package io.pkts.diameter.codegen;

import org.xml.sax.Locator;

/**
 * Contains common checks for null etc. All assertXXXX will throw a {@link CodeGenParseException}. All checkXXX will
 * return a boolean and all ensureXXX will throw an {@link IllegalArgumentException}.
 * <p>
 * Note that assertXXX and enxureXXX are the exact same thing except that they throw different
 * exception. The only reason for this is that sometimes I want to throw a {@link CodeGenParseException}
 * and sometimes I want to throw an {@link IllegalArgumentException}.
 *
 * @author jonas@jonasborjesson.com
 */
public final class PreConditions {

    private PreConditions() {
    }

    public static <T> T ensureNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static <T> T ensureNotNull(final T reference) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return reference;
    }

    public static <T> T assertNotNull(final T reference, final String msg, final Locator locator) throws CodeGenParseException {
        if (reference == null) {
            throw new CodeGenParseException(locator, msg);
        }
        return reference;
    }

    public static <T> T assertNotNull(final T reference, final Locator locator) throws CodeGenParseException {
        if (reference == null) {
            throw new CodeGenParseException(locator, "Calue cannot be null");
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

    public static String ensureNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static void ensureArgument(final boolean expression, final String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static String assertNotEmpty(final String reference, final String msg, final Locator locator) throws CodeGenParseException {
        if (reference == null || reference.isEmpty()) {
            throw new CodeGenParseException(locator, msg);
        }
        return reference;
    }

    public static void assertArgument(final boolean expression, final String msg, final Locator locator) throws CodeGenParseException {
        if (!expression) {
            throw new CodeGenParseException(locator, msg);
        }
    }

}
