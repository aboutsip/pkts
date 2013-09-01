/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.impl.SipInitialLine;
import io.pkts.packet.sip.impl.SipRequestImpl;
import io.pkts.packet.sip.impl.SipResponseImpl;

import java.io.IOException;

/**
 * a SIP framer that operates on a set of data and simply just finds the
 * boundaries of the SIP message.
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipFramer {

    /**
     * Frame the supplied buffer into a {@link SipMessage}. No deep analysis of
     * the message will be performed by this framer so there is no guarantee
     * that this {@link SipMessage} is actually a well formed message.
     * 
     * @param buffer
     * @return the framed {@link SipMessage}
     */
    public static SipMessage frame(final Buffer buffer) throws IOException {
        if (!couldBeSipMessage(buffer)) {
            throw new SipParseException(0, "Cannot be a SIP message because is doesnt start with \"SIP\" "
                    + "(for responses) or a method (for requests)");
        }

        // we just assume that the initial line
        // indeed is a correct sip line
        final Buffer rawInitialLine = buffer.readLine();

        // which means that the headers are about
        // to start now.
        final int startHeaders = buffer.getReaderIndex();

        Buffer currentLine = null;
        while ((currentLine = buffer.readLine()) != null && currentLine.hasReadableBytes()) {
            // just moving along, we don't really care why
            // we stop, we have found what we want anyway, which
            // is the boundary between headers and the potential
            // payload (or end of message)
        }

        final Buffer headers = buffer.slice(startHeaders, buffer.getReaderIndex());
        Buffer payload = null;
        if (buffer.hasReadableBytes()) {
            payload = buffer.slice();
        }

        if (SipInitialLine.isResponseLine(rawInitialLine)) {
            return new SipResponseImpl(rawInitialLine, headers, payload);
        } else {
            return new SipRequestImpl(rawInitialLine, headers, payload);
        }
    }

    /**
     * Helper function that checks whether or not the data could be a SIP
     * message. It is a very basic check but if it doesn't go through it
     * definitely is not a SIP message.
     * 
     * @param data
     * @return
     */
    public static boolean couldBeSipMessage(final Buffer data) throws IOException {
        final byte a = data.getByte(0);
        final byte b = data.getByte(1);
        final byte c = data.getByte(2);
        return a == 'S' && b == 'I' && c == 'P' || // response
                a == 'I' && b == 'N' && c == 'V' || // INVITE
                a == 'A' && b == 'C' && c == 'K' || // ACK
                a == 'B' && b == 'Y' && c == 'E' || // BYE
                a == 'O' && b == 'P' && c == 'T' || // OPTIONS
                a == 'C' && b == 'A' && c == 'N' || // CANCEL
                a == 'M' && b == 'E' && c == 'S' || // MESSAGE
                a == 'R' && b == 'E' && c == 'G' || // REGISTER
                a == 'I' && b == 'N' && c == 'F' || // INFO
                a == 'P' && b == 'R' && c == 'A' || // PRACK
                a == 'S' && b == 'U' && c == 'B' || // SUBSCRIBE
                a == 'N' && b == 'O' && c == 'T' || // NOTIFY
                a == 'U' && b == 'P' && c == 'D' || // UPDATE
                a == 'R' && b == 'E' && c == 'F' || // REFER
                a == 'P' && b == 'U' && c == 'B'; // PUBLISH

    }

}
