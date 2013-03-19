/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import java.io.IOException;
import java.util.List;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.header.Parameters;
import com.aboutsip.yajpcap.packet.sip.header.ViaHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipParser;

/**
 * Not extending the {@link ParametersImpl} because the way we parse the
 * Via-header we have already parsed the parameters. This because the Via-header
 * requires the branch parameter to be there and as such the framing of the
 * Via-header is done in a way that would complain if there are no params etc.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class ViaHeaderImpl implements ViaHeader, SipHeader, Parameters {

    private static final Buffer BRANCH = Buffers.wrap("branch");
    private static final Buffer RECEIVED = Buffers.wrap("received");
    private static final Buffer RPORT = Buffers.wrap("rport");
    private static final Buffer TTL = Buffers.wrap("ttl");

    /**
     * The original Via-header.
     */
    private final Buffer original;

    private final Buffer transport;

    private final Buffer host;

    private final Buffer rawPort;

    private int port = -2; // negative two indicates we haven't set it yet.

    /**
     * Contains a list of all the parameters. It may be more efficient to keep
     * them in a map but since there won't be that many I wouldn't bet too much
     * money on it. Sometimes simple is easier. Will probably do some
     * performance testing on that just out of curiosity at some point.
     */
    private final List<Buffer[]> params;

    private int indexOfBranch = -1;

    private int indexOfReceived = -1;

    private int indexOfRPort = -1;

    private final int indexOfTTL = -1;

    /**
     * 
     */
    private ViaHeaderImpl(final Buffer original, final Buffer transport, final Buffer host, final Buffer port,
            final List<Buffer[]> params) {
        this.original = original;
        this.transport = transport;
        this.host = host;
        this.rawPort = port;
        if (this.rawPort == null) {
            this.port = -1;
        }

        this.params = params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException, IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getParameter(final String name) throws SipParseException, IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getName() {
        return ViaHeader.NAME;
    }

    @Override
    public Buffer getTransport() {
        return this.transport;
    }

    @Override
    public int getTTL() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        return this.original;
    }

    @Override
    public Buffer getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        if (this.port == -2) {
            // TODO: perhaps save it plus implement my own
            // to string function in buffer
            this.port = Integer.parseInt(this.rawPort.toString());
        }

        return this.port;
    }

    @Override
    public Buffer getReceived() {
        if (this.indexOfReceived == -1) {
            this.indexOfReceived = findParameter(RECEIVED);
        }
        if (this.indexOfReceived == -1) {
            return null;
        }
        return this.params.get(this.indexOfBranch)[1];
    }

    @Override
    public boolean hasRPort() {
        if (this.indexOfRPort == -1) {
            this.indexOfRPort = findParameter(RPORT);
        }
        if (this.indexOfRPort == -1) {
            return false;
        }
        return true;
    }

    @Override
    public int getRPort() {
        if (this.indexOfRPort == -1) {
            this.indexOfRPort = findParameter(RPORT);
        }
        if (this.indexOfRPort == -1) {
            return -1;
        }
        final Buffer port = this.params.get(this.indexOfRPort)[1];
        if (port == null) {
            return -1;
        }
        // TODO: perhaps save it plus implement my own
        // to string function in buffer
        return Integer.parseInt(port.toString());
    }

    @Override
    public Buffer getBranch() {
        if (this.indexOfBranch == -1) {
            this.indexOfBranch = findParameter(BRANCH);
        }
        if (this.indexOfBranch == -1) {
            return null;
        }
        return this.params.get(this.indexOfBranch)[1];
    }

    private int findParameter(final Buffer param) {
        for (int i = 0; i < this.params.size(); ++i) {
            final Buffer[] keyValue = this.params.get(i);
            if (keyValue[0].equals(param)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isUDP() {
        try {
            final Buffer t = this.transport;
            return t.capacity() == 3 && t.getByte(0) == 'U' && t.getByte(1) == 'D' && t.getByte(2) == 'P';
        } catch (final IOException e) {
            throw new RuntimeException("Got IOException when examining the transport. Should not be possible", e);
        }
    }

    @Override
    public boolean isTCP() {
        try {
            final Buffer t = this.transport;
            return t.capacity() == 3 && t.getByte(0) == 'T' && t.getByte(1) == 'C' && t.getByte(2) == 'P';
        } catch (final IOException e) {
            throw new RuntimeException("Got IOException when examining the transport. Should not be possible", e);
        }
    }

    @Override
    public boolean isTLS() {
        try {
            final Buffer t = this.transport;
            return t.capacity() == 3 && t.getByte(0) == 'T' && t.getByte(1) == 'L' && t.getByte(2) == 'S';
        } catch (final IOException e) {
            throw new RuntimeException("Got IOException when examining the transport. Should not be possible", e);
        }
    }

    @Override
    public boolean isSCTP() {
        try {
            final Buffer t = this.transport;
            return t.capacity() == 4 && t.getByte(0) == 'S' && t.getByte(1) == 'C' && t.getByte(2) == 'T'
                    && t.getByte(3) == 'P';
        } catch (final IOException e) {
            throw new RuntimeException("Got IOException when examining the transport. Should not be possible", e);
        }
    }

    /**
     * Frame a buffer into a {@link ViaHeader}.
     * 
     * NOTE, this method assumes that you have already stripped off the header
     * name "Via".
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     */
    public static ViaHeader frame(final Buffer buffer) throws SipParseException {
        try {
            final Buffer original = buffer.slice();
            final Object[] result = SipParser.consumeVia(buffer);
            final Buffer transport = (Buffer) result[0];
            final Buffer host = (Buffer) result[1];
            final Buffer port = result[2] == null ? null : (Buffer) result[2];
            final List<Buffer[]> params = (List<Buffer[]>) result[3];
            return new ViaHeaderImpl(original, transport, host, port, params);
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to frame the Via header due to IOException", e);
        }
    }

    /**
     * For a Via-header make sure that the branch parameter is present.
     * 
     * {@inheritDoc}
     */
    @Override
    public void verify() throws SipParseException {
        if (getBranch() == null) {
            throw new SipParseException(0, "Did not find the mandatory branch parameter. Via is illegal");
        }

    }

    @Override
    public String toString() {
        return "Via: " + this.original.toString();
    }

}
