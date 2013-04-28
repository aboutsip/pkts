/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.ByteNotFoundException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class SipInitialLine extends SipParser {

    protected SipInitialLine() {
        // left empty intentionally
    }

    /**
     * The request initial line as a raw buffer.
     * 
     * @return
     */
    public abstract Buffer getBuffer();

    /**
     * Parse the buffer into a SIP initial line, which either can be a
     * {@link SipRequestLine} or a {@link SipResponseLine}.
     * 
     * The parsing will only check so that a few things are correct but in
     * general it doesn't do any deeper analysis of the initial line. To make
     * sure that the resulting sip message actually is correct, call the
     * {@link SipMessage#verify()} method, which will do a deeper analysis of
     * the sip message
     * 
     * @param buffer
     * @return
     */
    public static final SipInitialLine parse(final Buffer buffer) throws SipParseException {
        Buffer part1 = null;
        Buffer part2 = null;
        Buffer part3 = null;
        try {
            part1 = buffer.readUntil(SipParser.SP);
            part2 = buffer.readUntil(SipParser.SP);
            part3 = buffer.readLine();

            if (SipParser.SIP2_0.equals(part1)) {
                final int statusCode = Integer.parseInt(part2.toString());
                return new SipResponseLine(statusCode, part3);
            }

            // not a response so then the last part must be the SIP/2.0
            // otherwise this is not a valid SIP initial line
            expectSIP2_0(part3);

            return new SipRequestLine(part1, part2);

        } catch (final NumberFormatException e) {
            final int index = buffer.getReaderIndex() - part3.capacity() - part2.capacity() - 1;
            throw new SipParseException(index, "unable to parse the SIP response code as an integer");
        } catch (final ByteNotFoundException e) {
            throw new SipParseException(buffer.getReaderIndex(), "expected space");
        } catch (final SipParseException e) {
            // is only thrown by the expectSIP2_0. Calculate the correct
            // index into the buffer
            final int index = buffer.getReaderIndex() - part3.capacity() + e.getErroOffset() - 1;
            throw new SipParseException(index, "Wrong SIP version");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "could not read from stream", e);
        }
    }

    public boolean isResponseLine() {
        return false;
    }

    public boolean isRequestLine() {
        return false;
    }

    /**
     * Write the bytes representing this {@link SipInitialLine} into the
     * destination {@link Buffer}.
     * 
     * @param dst
     */
    public abstract void getBytes(Buffer dst);

    @Override
    protected abstract SipInitialLine clone();

}
