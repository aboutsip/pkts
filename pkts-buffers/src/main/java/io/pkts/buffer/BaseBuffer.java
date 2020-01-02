package io.pkts.buffer;

import java.io.IOException;

/**
 * Abstract abstract buffer :-)
 *
 * @author jonas@jonasborjesson.com
 */
public abstract class BaseBuffer implements Buffer {
    protected static final byte LF = '\n';
    protected static final byte CR = '\r';

    @Override
    public abstract Buffer clone();

    @Override
    public boolean hasWritableBytes() {
        return getWritableBytes() > 0;
    }

    /**
     * Check whether we have enough space for writing the desired length.
     *
     * @param length
     * @return
     */
    protected boolean checkWritableBytesSafe(final int length) {
        return getWritableBytes() >= length;
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     *
     * @param length
     *            the length the user wishes to read
     * @throws IndexOutOfBoundsException
     *             in case we don't have the bytes available
     */
    protected void checkReadableBytes(final int length) throws IndexOutOfBoundsException {
        if (!checkReadableBytesSafe(length)) {
            throw new IndexOutOfBoundsException("Not enough readable bytes");
        }
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     *
     * @param length
     *            the length the user wishes to read
     * @return true if we have enough bytes available for read
     */
    protected boolean checkReadableBytesSafe(final int length) {
        return getReadableBytes() >= length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Buffer readUntil(final byte b) throws IOException, ByteNotFoundException {
        return readUntil(4096, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Buffer readUntil(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
            IllegalArgumentException {
        final Buffer result = readUntilSafe(maxBytes, bytes);
        if (result == null) {
            throw new ByteNotFoundException(bytes);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Buffer readUntilSafe(final int maxBytes, final byte... bytes) throws IOException, IllegalArgumentException {
        final int index = indexOf(maxBytes, bytes);
        if (index == -1) {
            return null;
        }

        final int size = index - getReaderIndex();
        final Buffer result;
        if (size == 0) {
            result = Buffers.EMPTY_BUFFER;
        } else {
            result = readBytes(size);
        }

        readByte(); // consume the one at the index as well
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int indexOf(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
            IllegalArgumentException {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("No bytes specified. Not sure what you want me to look for");
        }

        final int start = getReaderIndex();
        int index = -1;

        while (hasReadableBytes() && getReaderIndex() - start < maxBytes && index == -1) {
            if (isByteInArray(readByte(), bytes)) {
                index = getReaderIndex() - 1;
            }
        }

        setReaderIndex(start);

        if (getReaderIndex() - start >= maxBytes) {
            throw new ByteNotFoundException(maxBytes, bytes);
        }

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int indexOf(final byte b) throws IOException, ByteNotFoundException, IllegalArgumentException {
        return this.indexOf(4096, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Buffer readLine() throws IOException {
        final int start = getReaderIndex();
        boolean foundCR = false;
        while (hasReadableBytes()) {
            final byte b = readByte();
            switch (b) {
                case LF:
                    return slice(start, getReaderIndex() - (foundCR ? 2 : 1));
                case CR:
                    foundCR = true;
                    break;
                default:
                    if (foundCR) {
                        setReaderIndex(getReaderIndex() - 1);
                        // return slice(start, this.lowerBoundary + this.readerIndex - 1);
                        return slice(start, getReaderIndex() - 1);
                    }
            }
        }

        // i guess there were nothing for us to read
        if (start >= getReaderIndex()) {
            return null;
        }

        return slice(start, getReaderIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Buffer readUntilSingleCRLF() throws IOException {
        final int start = getReaderIndex();
        int found = 0;
        while (found < 2 && hasReadableBytes()) {
            final byte b = readByte();
            if (found == 0 && b == CR) {
                ++found;
            } else if (found == 1 && b == LF) {
                ++found;
            } else {
                found = 0;
            }
        }
        if (found == 2) {
            return slice(start, getReaderIndex() - 2);
        } else {
            setReaderIndex(start);
            return null;
        }
    }

    @Override
    public final Buffer readUntilDoubleCRLF() throws IOException {
        final int start = getReaderIndex();
        int found = 0;
        while (found < 4 && hasReadableBytes()) {
            final byte b = readByte();
            if ((found == 0 || found == 2) && b == CR) {
                ++found;
            } else if ((found == 1 || found == 3) && b == LF) {
                ++found;
            } else {
                found = 0;
            }
        }
        if (found == 4) {
            return slice(start, getReaderIndex() - 4);
        } else {
            setReaderIndex(start);
            return null;
        }
    }

    @Override
    public final int parseToInt() throws NumberFormatException, IOException {
        return parseToInt(10);
    }

    /**
     * (Copied from the Integer class and slightly altered to read from this
     * buffer instead of a String)
     *
     * Parses the string argument as a signed integer in the radix specified by
     * the second argument. The characters in the string must all be digits of
     * the specified radix (as determined by whether
     * {@link java.lang.Character#digit(char, int)} returns a nonnegative
     * value), except that the first character may be an ASCII minus sign
     * <code>'-'</code> (<code>'&#92;u002D'</code>) to indicate a negative
     * value. The resulting integer value is returned.
     * <p>
     * An exception of type <code>NumberFormatException</code> is thrown if any
     * of the following situations occurs:
     * <ul>
     * <li>The first argument is <code>null</code> or is a string of length
     * zero.
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or larger than
     * {@link java.lang.Character#MAX_RADIX}.
     * <li>Any character of the string is not a digit of the specified radix,
     * except that the first character may be a minus sign <code>'-'</code> (
     * <code>'&#92;u002D'</code>) provided that the string is longer than length
     * 1.
     * <li>The value represented by the string is not a value of type
     * <code>int</code>.
     * </ul>
     * <p>
     * Examples: <blockquote>
     *
     * <pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre>
     *
     * </blockquote>
     *
     * @param radix
     *            the radix to be used while parsing <code>s</code>.
     * @return the integer represented by the string argument in the specified
     *         radix.
     * @exception NumberFormatException
     *                if the <code>String</code> does not contain a parsable
     *                <code>int</code>.
     */
    @Override
    public final int parseToInt(final int radix) throws NumberFormatException, IOException {
        if (getReadableBytes() == 0) {
            throw new NumberFormatException("Buffer is empty, cannot convert it to an integer");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = getReaderIndex();

        final int max = getReadableBytes() + getReaderIndex();
        final int limit;
        final int multmin;
        int digit;

        if (max > 0) {
            if (getByte(i) == (byte) '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = Character.digit((char) getByte(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) getByte(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + this + "\"");
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException("For input string: \"" + this + "\"");
            }
        } else {
            return -result;
        }
    }

    protected static boolean isByteInArray(final byte b, final byte[] bytes) {
        for (final byte x : bytes) {
            if (x == b) {
                return true;
            }
        }
        return false;
    }
}
