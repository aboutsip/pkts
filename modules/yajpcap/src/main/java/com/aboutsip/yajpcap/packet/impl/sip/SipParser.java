/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipParser {

    public static final Buffer SIP2_0 = Buffers.wrap("SIP/2.0");

    public static final byte SP = ' ';

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

}
