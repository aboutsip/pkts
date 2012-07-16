/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SIPFramer implements Framer {

    private final FramerManager framerManager;

    public SIPFramer(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.SIP;
    }

    /**
     * {@inheritDoc}
     * 
     * Very basic way of framing a sip message. It makes a lot of assumption but
     * in the framing phase we are, well, just framing.
     */
    @Override
    public Frame frame(final Buffer buffer) throws IOException {

        // we just assume that the initial line
        // indeed is a correct sip line
        final Buffer initialLine = buffer.readLine();

        // which means that the headers are about
        // to start now.
        final int startHeaders = buffer.getReaderIndex();

        Buffer currentLine = null;
        while (((currentLine = buffer.readLine()) != null) && currentLine.hasReadableBytes()) {
            // just moving along, we don't really care why
            // we stop, we have found what we want anyway, which
            // is the boundary between headers and the potential
            // payload (or end of message)
        }

        final Buffer headers = buffer.slice(startHeaders, buffer.getReaderIndex());
        final Buffer payload = buffer.slice(); // may actually be empty

        return new SipFrame(this.framerManager, initialLine, headers, payload);
    }

    @Override
    public boolean accept(final Buffer data) throws IOException {
        // if the first three bytes matches anything if the first three bytes
        // matches anything of the below stuff, then it is likely to be a SIP
        // message in this data.

        // NOTE: it is not fool proof and we may in the future
        // want to check a little more before saying anything

        final byte a = data.getByte(0);
        final byte b = data.getByte(1);
        final byte c = data.getByte(2);
        return ((a == 'S') && (b == 'I') && (c == 'P')) || // response
                ((a == 'I') && (b == 'N') && (c == 'V')) || // INVITE
                ((a == 'A') && (b == 'C') && (c == 'K')) || // ACK
                ((a == 'B') && (b == 'Y') && (c == 'E')) || // BYE
                ((a == 'O') && (b == 'P') && (c == 'T')) || // OPTIONS
                ((a == 'C') && (b == 'A') && (c == 'N')) || // CANCEL
                ((a == 'M') && (b == 'E') && (c == 'S')) || // MESSAGE
                ((a == 'R') && (b == 'E') && (c == 'G')) || // REGISTER
                ((a == 'I') && (b == 'N') && (c == 'F')) || // INFO
                ((a == 'P') && (b == 'R') && (c == 'A')) || // PRACK
                ((a == 'S') && (b == 'U') && (c == 'B')) || // SUBSCRIBE
                ((a == 'N') && (b == 'O') && (c == 'T')) || // NOTIFY
                ((a == 'U') && (b == 'P') && (c == 'D')) || // UPDATE
                ((a == 'R') && (b == 'E') && (c == 'F')) || // REFER
                ((a == 'P') && (b == 'U') && (c == 'B')); // PUBLISH
    }

}
