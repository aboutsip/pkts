/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.Transport;
import io.pkts.packet.sip.header.Parameters;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Not extending the {@link ParametersImpl} because the way we parse the
 * Via-header we have already parsed the parameters. This because the Via-header
 * requires the branch parameter to be there and as such the framing of the
 * Via-header is done in a way that would complain if there are no params etc.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class ViaHeaderImpl implements ViaHeader, SipHeader, Parameters {

    /**
     * The original Via-header and since a header is immutable, all the pre-parsed
     * values are just there for convenience.
     */
    private final Buffer original;

    private final Transport transport;

    private final Buffer host;

    private final int port;

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

    /**
     *
     * @param via the actual value of the via header, which cannot be changed and is what
     *            we will be using when transferring this value to external sources. Note, it says the VALUE of the
     *            Via-header, hence the actual header name ("Via") must NOT be part of this buffer.
     * @param transport the parsed transport
     * @param host the parsed host
     * @param port the parsed port or negative 1 if it isn't set.
     * @param params all the parameters such as the branch and the rport parameter
     * @param indexOfBranch the index of the branch parameter
     * @param indexOfReceived the index of the received parameter
     * @param indexOfRPort the index of the rport parameter
     */
    public ViaHeaderImpl(final Buffer via,
                         final Transport transport,
                         final Buffer host,
                         final int port,
                         final List<Buffer[]> params,
                         final int indexOfBranch,
                         final int indexOfReceived,
                         final int indexOfRPort) {
        this.original = via;
        this.transport = transport;
        this.host = host;
        this.port = port;
        this.params = Collections.unmodifiableList(params);
        this.indexOfBranch = indexOfBranch;
        this.indexOfReceived = indexOfReceived;
        this.indexOfRPort = indexOfRPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException, IllegalArgumentException {
        final int index = findParameter(name);
        if (index == -1) {
            return null;
        }

        return this.params.get(index)[1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getParameter(final String name) throws SipParseException, IllegalArgumentException {
        return getParameter(Buffers.wrap(name));
    }

    @Override
    public void setParameter(Buffer name, Buffer value) throws SipParseException, IllegalArgumentException {
        throw new IllegalArgumentException("Forbidden");
    }

    @Override
    public void setParameter(Buffer name, Supplier<Buffer> value) throws SipParseException, IllegalArgumentException {
        throw new IllegalArgumentException("Forbidden");
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
        return this.transport.toUpperCaseBuffer();
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
        return this.port;
    }

    @Override
    public Buffer getReceived() {
        if (this.indexOfReceived == -1) {
            return null;
        }
        return this.params.get(this.indexOfReceived)[1];
    }

    @Override
    public boolean hasRPort() {
        if (this.indexOfRPort == -1) {
            return false;
        }
        return true;
    }

    @Override
    public int getRPort() {
        if (this.indexOfRPort == -1) {
            return -1;
        }
        final Buffer port = this.params.get(this.indexOfRPort)[1];
        if (port == null) {
            return -1;
        }
        // TODO: perhaps save it plus implement my own
        // to string function in buffer
        try {
            return port.parseToInt();
        } catch (final NumberFormatException e) {
            return -1;
        } catch (final IOException e) {
            return -1;
        }
    }

    @Override
    public Buffer getBranch() {
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
        return transport == Transport.udp;
    }

    @Override
    public boolean isTCP() {
        return transport == Transport.tcp;
    }

    @Override
    public boolean isTLS() {
        return transport == Transport.tls;
    }

    @Override
    public boolean isSCTP() {
        return transport == Transport.sctp;
    }

    @Override
    public boolean isWS() {
        return transport == Transport.ws;
    }

    @Override
    public boolean isWSS() {
        return transport == Transport.wss;
    }

    /**
     * For a Via-header make sure that the branch parameter is present.
     * 
     * {@inheritDoc}
     */
    @Override
    public void verify() throws SipParseException {
        // A via header can never be constructed
        // in a faulty manner.
    }

    @Override
    public String toString() {
        return NAME.toString() + ": " + original.toString();
    }

    @Override
    public void getBytes(final Buffer dst) {
        NAME.getBytes(0, dst);
        dst.write(SipParser.COLON);
        dst.write(SipParser.SP);
        this.original.getBytes(0, dst);
    }

    @Override
    public ViaHeader clone() {
        return new ViaHeaderImpl(original, transport, host, port, params, indexOfBranch, indexOfReceived, indexOfRPort);
    }

    @Override
    public ViaHeader ensure() {
        return this;
    }

    @Override
    public ViaHeader.Builder copy() {
        return new ViaHeader.Builder(transport, host, port, new ArrayList<>(params), indexOfBranch, indexOfReceived, indexOfRPort);
    }

}
