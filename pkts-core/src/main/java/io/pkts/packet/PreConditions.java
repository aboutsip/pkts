package io.pkts.packet;

import java.util.Collection;

/**
 * Note: as pkts.io will move to sniceio instead, we will use the PreConditions class
 * that exists within sniceio-commons. The current one within pkts.io is very SIP focused
 * and since we really cannot use it for the general use case, and since the port to pkts.io will
 * be breaking changes and as such, requires a major version bump, we will copy paste the one from
 * sniceio-commons to here for now. Not great but better than writing manual "asserts" all the time.
 */
public class PreConditions {
    private PreConditions() {
    }

    public static <T> void assertNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference != null) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static <T> void assertNull(final T reference) throws IllegalArgumentException {
        assertNull(reference, "Value must be null");
    }

    public static <T> T assertNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        } else {
            return reference;
        }
    }

    public static <T> T assertNotNull(final T reference) throws IllegalArgumentException {
        return assertNotNull(reference, "Value cannot be null");
    }

    public static String assertNotEmpty(final String reference) throws IllegalArgumentException {
        return assertNotEmpty(reference, "The argument cannot be null or the empty string");
    }

    public static String assertNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference != null && !reference.isEmpty()) {
            return reference;
        } else {
            throw new IllegalArgumentException(msg);
        }
    }

    public static <T> Collection<T> assertCollectionNotEmpty(final Collection<T> reference) throws IllegalArgumentException {
        return assertCollectionNotEmpty(reference, "The argument cannot be null or an empty collection");
    }

    public static <T> Collection<T> assertCollectionNotEmpty(final Collection<T> reference, final String msg) throws IllegalArgumentException {
        if (reference != null && !reference.isEmpty()) {
            return reference;
        } else {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertArgument(final boolean expression, final String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertArgument(final boolean expression) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void assertArray(final byte[] array, final int offset, final int length, final String msg) throws IllegalArgumentException {
        if (array == null) {
            throw new IllegalArgumentException(msg);
        } else {
            assertArrayBoundaries(array.length, offset, length, msg);
        }
    }

    public static void assertArray(final byte[] array) throws IllegalArgumentException {
        if (array == null) {
            throw new IllegalArgumentException("The byte array cannot be null");
        } else {
            assertArray(array, 0, array.length);
        }
    }

    private static void assertArrayBoundaries(final int arraySize, final int offset, final int length, final String msg) throws IllegalArgumentException {
        if (length > arraySize || offset < 0 || offset != 0 && offset >= arraySize || offset + length > arraySize || length < 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertArray(final byte[] array, final int offset, final int length) throws IllegalArgumentException {
        assertArray(array, offset, length, "The byte-array and the offset and/or length does not match up");
    }

    public static <T> T ensureNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        } else {
            return reference;
        }
    }

    public static <T> T ensureNotNull(final T reference) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException("Value cannot be null");
        } else {
            return reference;
        }
    }

    public static String ensureNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference != null && !reference.isEmpty()) {
            return reference;
        } else {
            throw new IllegalArgumentException(msg);
        }
    }

    public static boolean checkIfEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean checkIfNotEmpty(final String string) {
        return !checkIfEmpty(string);
    }

    public static <T> T ifNull(final T reference, final T defaultValue) {
        return reference == null ? defaultValue : reference;
    }
}
