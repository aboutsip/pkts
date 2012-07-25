/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipParser {

    public static final Buffer SIP2_0 = Buffers.wrap("SIP/2.0");

    public static final byte COLON = ':';

    public static final byte CR = '\r';

    public static final byte LF = '\n';

    public static final byte SP = ' ';

    public static final byte HTAB = '\t';

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // -------- Expect methods expects something to be true and if not ------
    // -------- they will throw an exception of some sort -------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Expect that the next set of bytes is "SIP/2.0" and if not then we will
     * throw a
     * 
     * @param buffer
     */
    public static void expectSIP2_0(final Buffer buffer) throws SipParseException {
        expect(buffer, 'S');
        expect(buffer, 'I');
        expect(buffer, 'P');
        expect(buffer, '/');
        expect(buffer, '2');
        expect(buffer, '.');
        expect(buffer, '0');
    }

    /**
     * 
     * @return
     */
    public static Buffer expectMethod(final Buffer buffer) {
        return null;
    }

    /**
     * Will check whether the next readable byte in the buffer is a certain byte
     * 
     * @param buffer the buffer to peek into
     * @param b the byte we are checking is the next byte in the buffer to be
     *            read
     * @return true if the next byte to read indeed is what we hope it is
     */
    public static boolean isNext(final Buffer buffer, final byte b) throws IOException {
        if (buffer.hasReadableBytes()) {
            buffer.markReaderIndex();
            final Byte actual = buffer.readByte();
            buffer.resetReaderIndex();
            return actual == b;
        }

        return false;
    }

    /**
     * Convenience method for expecting (and consuming) a HCOLON, which is
     * defined as:
     * 
     * HCOLON = *( SP / HTAB ) ":" SWS
     * 
     * See RFC3261 section 25.1 Basic Rules
     */
    public static void expectHCOLON(final Buffer buffer) throws SipParseException {
        try {
            consumeWS(buffer);
            expect(buffer, COLON);
            consumeSWS(buffer);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Expect the next byte to be a white space
     * 
     * @param buffer
     */
    public static void expectWS(final Buffer buffer) throws SipParseException {
        try {
            if (buffer.hasReadableBytes()) {

                final byte b = buffer.getByte(buffer.getReaderIndex());
                if ((b == SP) || (b == HTAB)) {
                    // ok, it was a WS so consume the byte
                    buffer.readByte();
                } else {
                    throw new SipParseException(buffer.getReaderIndex(), "Expected WS");
                }
            } else {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Expected WS but nothing more to read in the buffer");
            }
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Consume sep (separating?) whitespace (LWS), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * SWS = [LWS] ; sep whitespace
     * 
     * "The SWS construct is used when linear white space is optional, generally
     * between tokens and separators." (RFC3261)
     * 
     * @param buffer
     * @return
     */
    public static boolean consumeSWS(final Buffer buffer) {
        try {
            return consumeLWS(buffer);
        } catch (final SipParseException e) {
            // ignore since currently the consumeLWS will ONLY
            // throw this exception when it expected a WS at a
            // particular place and since SWS is all optional
            // we will silently just consume it, but return
            // false however
            return false;
        }
    }

    /**
     * Consume linear whitespace (LWS), which according to RFC3261 section 25.1
     * Basic Rules is:
     * 
     * LWS = [*WSP CRLF] 1*WSP ; linear whitespace
     * 
     * @param buffer
     * @return true if we indeed did consume some LWS
     */
    public static boolean consumeLWS(final Buffer buffer) throws SipParseException {
        final int i = buffer.getReaderIndex();
        consumeWS(buffer);

        // if we consume a CRLF we expect at least ONE WS to be present next
        if (consumeCRLF(buffer)) {
            expectWS(buffer);
        }
        consumeWS(buffer);
        if (buffer.getReaderIndex() == i) {
            throw new SipParseException(i, "Expected at least 1 WSP");
        }
        return true;
    }

    /**
     * Consume CR + LF
     * 
     * @param buffer
     * @return true if we indeed did consume CRLF, false otherwise
     */
    public static boolean consumeCRLF(final Buffer buffer) throws SipParseException {
        try {
            buffer.markReaderIndex();
            final byte cr = buffer.readByte();
            final byte lf = buffer.readByte();
            if ((cr == CR) && (lf == LF)) {
                return true;
            }

            buffer.resetReaderIndex();
            return false;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Check so that the next byte in the passed in buffer is the expected one.
     * 
     * @param buffer the buffer that we will check.
     * @param expected the buffer that contains the expected byte (note, it is 1
     *            byte, not bytes)
     * @throws ParseException in case the expected byte is not the next byte in
     *             the buffer
     */
    public static void expect(final Buffer buffer, final byte expected) throws SipParseException, IOException {
        final byte actual = buffer.readByte();
        if (actual != expected) {
            final String actualStr = new String(new byte[] { actual });
            final String expectedStr = new String(new byte[] { expected });
            throw new SipParseException(buffer.getReaderIndex(), "Expected '" + expected + "' (" + expectedStr
                    + ") got '" + actual + "' (" + actualStr + ")");
        }
    }

    /**
     * Consume all the whitespace we find (WS)
     * 
     * @param buffer
     * @return true if we did consume something, false otherwise
     */
    public static boolean consumeWS(final Buffer buffer) throws SipParseException {
        try {
            boolean consumed = false;
            boolean done = false;
            while (buffer.hasReadableBytes() && !done) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    buffer.readByte();
                    consumed = true;
                } else {
                    done = true;
                }
            }
            return consumed;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Check so that the next byte in the passed in buffer is the expected one.
     * 
     * @param buffer the buffer that we will check.
     * @param expected the expected char
     * @throws SipParseException in case the expected char is not the next char
     *             in the buffer or if there is an error reading from the
     *             underlying stream
     */
    public static void expect(final Buffer buffer, final char ch) throws SipParseException {
        try {
            final int i = buffer.readUnsignedByte();
            if (i != ch) {
                throw new SipParseException(buffer.getReaderIndex(), "Expected '" + ch + "' got '" + i + "'");
            }
        }
        catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     */
    public static Buffer nextHeader(final Buffer buffer) throws SipParseException {
        try {

            final int startIndex = buffer.getReaderIndex();
            int nameIndex = 0;
            while (buffer.hasReadableBytes() && (nameIndex == 0)) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB) || isNext(buffer, COLON)) {
                    nameIndex = buffer.getReaderIndex();
                } else {
                    buffer.readByte();
                }
            }

            // Bad header! No HCOLON found! (or beginning thereof anyway)
            if (nameIndex == 0) {
                throw new SipParseException(buffer.getReaderIndex(), "Expected HCOLON");
            }

            // final String name = buffer.copy(startIndex, nameIndex -
            // startIndex).toString(UTF8);
            final Buffer name = buffer.slice(startIndex, nameIndex);

            expectHCOLON(buffer);

            // NOTE: all of this stuff can be done so much better!!! Get rid of
            // the
            // stupid
            // "do one thing at a time and the concatenate it all" stuff...

            // Note, we are framing headers from a sip request or response which
            // is
            // why we safely can assume that there will ALWAYS be a CRLF after
            // each
            // line
            final Buffer valueBuffer = buffer.readLine();

            // the header may be a folded one so check that and if so, consume
            // it
            // too
            final List<Buffer> foldedLines = new ArrayList<Buffer>();
            boolean done = false;
            while (!done) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    consumeWS(buffer);
                    foldedLines.add(buffer.readLine());
                } else {
                    done = true;
                }
            }
            String stupid = valueBuffer.toString();
            for (final Buffer line : foldedLines) {
                stupid += " " + line.toString();
            }

            // TODO: add a SipHeaderBuffer or something
            return Buffers.wrap((name.toString() + ": " + stupid).getBytes());
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

}
