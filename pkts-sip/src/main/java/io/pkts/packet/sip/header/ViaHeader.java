/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.Transport;
import io.pkts.packet.sip.header.impl.ViaHeaderImpl;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;

/**
 * Source rfc 3261 section 8.1.1.7
 * 
 * <p>
 * The Via header field indicates the transport used for the io.sipstack.transaction.transaction and
 * identifies the location where the response is to be sent. A Via header field
 * value is added only after the transport that will be used to reach the next
 * hop has been selected (which may involve the usage of the procedures in [4]).
 * </p>
 * 
 * <p>
 * When the UAC creates a request, it MUST insert a Via into that request. The
 * protocol name and protocol version in the header field MUST be SIP and 2.0,
 * respectively. The Via header field value MUST contain a branch parameter.
 * This parameter is used to identify the transaction created by that request.
 * This parameter is used by both the client and the server.
 * </p>
 * 
 * <p>
 * The branch parameter value MUST be unique across space and time for all
 * requests sent by the UA. The exceptions to this rule are CANCEL and ACK for
 * non-2xx responses. As discussed below, a CANCEL request will have the same
 * value of the branch parameter as the request it cancels. As discussed in
 * Section 17.1.1.3, an ACK for a non-2xx response will also have the same
 * branch ID as the INVITE whose response it acknowledges.
 * </p>
 * 
 * <p>
 * The uniqueness property of the branch ID parameter, to facilitate its use as
 * a io.sipstack.transaction.transaction ID, was not part of RFC 2543.
 * </p>
 * 
 * <p>
 * The branch ID inserted by an element compliant with this specification MUST
 * always begin with the characters "z9hG4bK". These 7 characters are used as a
 * magic cookie (7 is deemed sufficient to ensure that an older RFC 2543
 * implementation would not pick such a value), so that servers receiving the
 * request can determine that the branch ID was constructed in the fashion
 * described by this specification (that is, globally unique). Beyond this
 * requirement, the precise format of the branch token is
 * implementation-defined.
 * </p>
 * 
 * <p>
 * The Via header maddr, ttl, and sent-by components will be set when the
 * request is processed by the transport layer (Section 18).
 * </p>
 * 
 * <p>
 * Via processing for proxies is described in Section 16.6 Item 8 and Section
 * 16.7 Item 3.
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface ViaHeader extends Parameters, SipHeader {

    Buffer NAME = Buffers.wrap("Via");

    Buffer COMPACT_NAME = Buffers.wrap("v");

    /**
     * The protocol, which typically is "UDP", "TCP" or "TLS" but can really be
     * anything according to RFC3261.
     * 
     * @return
     */
    Buffer getTransport();

    Buffer getHost();

    int getPort();

    Buffer getReceived();

    @Override
    default ViaHeader toViaHeader() {
        return this;
    }

    @Override
    default boolean isViaHeader() {
        return true;
    }

    /**
     * For a request, the rport value will not be filled out since the
     * downstream element will do so when it discovers the rport parameter on a
     * {@link ViaHeader}. Hence, if you use {@link #getRPort()} you will not
     * correctly be able to determine whether this {@link ViaHeader} actually
     * has the rport parameter present or if it is simply not set yet. However,
     * this method will return true if the rport parameter exists on the
     * {@link ViaHeader}, irrespectively whether it has a value or not.
     * 
     * @return
     */
    boolean hasRPort();

    /**
     * Get the value of the rport parameter. -1 (negative one) will be returned
     * if the value is not set. Note, if you get -1 that doesn't mean that the
     * rport is not present on the {@link ViaHeader}. To make sure that the
     * {@link ViaHeader} indeed has the rport parameter set, use the
     * {@link #hasRPort()}.
     * 
     * @return
     */
    int getRPort();

    // void setRPort(int port);

    /**
     * The branch-parameter is mandatory and as such should always be there.
     * However, everything is done lazily in this library so there is not a 100%
     * guarantee that the branch header actually is present. Hence, you MUST be
     * prepared to check for null in case the Via-header is bad. If important to
     * your io.sipstack.application.application (and if you are building a stack it probably will be)
     * then please call {@link #verify()} on your headers since that will
     * guarantee that they conform to whatever the various RFC's mandates.
     * 
     * @return
     */
    Buffer getBranch();

    // void setBranch(Buffer branch);

    int getTTL();

    /**
     * Convenience method for checking whether the protocol is UDP or not.
     * 
     * @return
     */
    boolean isUDP();

    /**
     * Convenience method for checking whether the protocol is TCP or not.
     * 
     * @return
     */
    boolean isTCP();

    /**
     * Convenience method for checking whether the protocol is TLS or not.
     * 
     * @return
     */
    boolean isTLS();

    /**
     * Convenience method for checking whether the protocol is SCTP or not.
     * 
     * @return
     */
    boolean isSCTP();

    /**
     * Convenience method for checking whether the protocol is WS or not.
     *
     * @return
     */
    boolean isWS();

    /**
     * Convenience method for checking whether the protocol is WSS or not.
     *
     * @return
     */
    boolean isWSS();

    @Override
    ViaHeader clone();

    @Override
    ViaHeader.Builder copy();

    /**
     * Frame a buffer into a {@link ViaHeader}.
     * 
     * NOTE, this method assumes that you have already stripped off the header name "Via".
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     */
    static ViaHeader frame(final Buffer buffer) throws SipParseException {
        try {
            return new Builder(buffer).build();
            // final Buffer original = buffer.slice();
            // final Object[] result = SipParser.consumeVia(buffer);
            // final Buffer transport = (Buffer) result[0];
            // final Buffer host = (Buffer) result[1];
            // final Buffer port = result[2] == null ? null : (Buffer) result[2];
            // final List<Buffer[]> params = (List<Buffer[]>) result[3];
            // return new ViaHeaderImpl(original, transport, host, port, params);
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to frame the Via header due to IOException", e);
        }
    }


    /**
     * Generate a cryptographic
     * 
     * @return
     */
    static Buffer generateBranch() {
        // TODO: change to something else.
        return Buffers.wrap("z9hG4bK-" + UUID.randomUUID().toString());
    }

    static Builder builder() {
        return new Builder();
    }

    static Builder withHost(final Buffer host) {
        final Builder builder = new Builder();
        return builder.withHost(host);
    }

    static Builder withHost(final String host) {
        final Builder builder = new Builder();
        return builder.withHost(host);
    }

    class Builder implements SipHeader.Builder<ViaHeader> {

        private static final Buffer udp = Buffers.wrap("UDP");
        private static final Buffer tcp = Buffers.wrap("TCP");
        private static final Buffer tls = Buffers.wrap("TLS");
        private static final Buffer sctp = Buffers.wrap("SCTP");
        private static final Buffer ws = Buffers.wrap("WS");
        private static final Buffer wss = Buffers.wrap("WSS");

        private static final Buffer BRANCH = Buffers.wrap("branch");
        private static final Buffer RECEIVED = Buffers.wrap("received");
        private static final Buffer RPORT = Buffers.wrap("rport");
        private static final Buffer TTL = Buffers.wrap("ttl");

        private int indexOfBranch;

        private int indexOfReceived;

        private int indexOfRPort;

        private Transport transport;
        private Buffer host;
        private int port;
        private List<Buffer[]> params;

        private Buffer branch;

        public Builder() {
            params = new ArrayList<>(3);
            port = -1;
            this.indexOfBranch = -1;
            this.indexOfReceived = -1;
            this.indexOfRPort = -1;
        }

        public Builder(final Transport transport,
                       final Buffer host,
                       final int port,
                       final List<Buffer[]> params,
                       final int indexOfBranch,
                       final int indexOfReceived,
                       final int indexOfRPort) {
            this.transport = transport;
            this.host = host;
            this.port = port;
            this.params = params;
            this.indexOfBranch = indexOfBranch;
            this.indexOfReceived = indexOfReceived;
            this.indexOfRPort = indexOfRPort;
        }

        public Builder(final Buffer buffer) throws IOException {
            try {
                final Object[] result = SipParser.consumeVia(buffer);
                this.transport = Transport.of((Buffer) result[0]);
                this.host = (Buffer) result[1];
                this.port = result[2] == null ? -1 : ((Buffer) result[2]).parseToInt();
                this.params = (List<Buffer[]>) result[3];
                this.indexOfBranch = findParameter(BRANCH);
                this.indexOfReceived = findParameter(RECEIVED);
                this.indexOfRPort = findParameter(RPORT);
            } catch (final IOException e) {
                throw new SipParseException(0, "Unable to frame the Via header due to IOException", e);
            }
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

        public Builder withParameter(final Buffer name, final Buffer value) throws SipParseException,
                IllegalArgumentException {
            final int index = findParameter(name);
            if (index == -1) {
                this.params.add(new Buffer[] { name, value });
            } else {
                this.params.get(index)[1] = value;
            }
            return this;
        }

        public Builder withParameter(final String name, final String value) throws SipParseException,
                IllegalArgumentException {
            return withParameter(Buffers.wrap(name), Buffers.wrap(value));
        }

        public Builder withPort(final int port) {
            assertArgument(port > 0, "Port must be greater than zero");
            this.port = port;
            return this;
        }

        public Builder withHost(final Buffer host) {
            this.host = assertNotEmpty(host, "Host cannot be empty or null");
            return this;
        }

        public Builder withHost(final String host) {
            assertNotEmpty(host, "Host cannot be empty or null");
            this.host = Buffers.wrap(host);
            return this;
        }

        /**
         * Convenience method for generating a default branch.
         * Same as calling: {@link ViaHeader#generateBranch()} and pass that into
         * {@link io.pkts.packet.sip.header.ViaHeader.Builder#withBranch(Buffer)}
         *
         * @return
         */
        public Builder withBranch() {
            return withBranch(ViaHeader.generateBranch());
        }

        public Builder withBranch(final Buffer branch) {
            assertNotEmpty(branch, "Branch cannot be empty or null.");
            if (this.indexOfBranch == -1) {
                this.indexOfBranch = findParameter(BRANCH);
            }
            if (this.indexOfBranch == -1) {
                this.indexOfBranch = this.params.size();
                this.params.add(new Buffer[]{BRANCH, branch});
            } else {
                this.params.get(this.indexOfBranch)[1] = branch;
            }
            return this;
        }

        public Builder withBranch(final String branch) {
            assertNotEmpty(branch, "Branch cannot be empty or null.");
            withBranch(Buffers.wrap(branch));
            return this;
        }

        /**
         * When you send out a request you typically want to add rport as a flag parameter
         * to indicate to the downstream element that it should fill it out.
         * @return
         */
        public Builder withRPortFlag() {
            return setRPort(-1);
        }

        /**
         * Check to see if this via we are building as the rport
         * parameter on it.
         *
         * @return
         */
        public boolean hasRPort() {
            if (this.indexOfRPort != -1) {
                return true;
            }

            this.indexOfRPort = findParameter(RPORT);
            if (this.indexOfRPort != -1) {
                return true;
            }

            return false;
        }

        public Builder withRPort(final int rport) {
            return setRPort(rport);
        }

        private Builder setRPort(final int rport) {
            if (this.indexOfRPort == -1) {
                this.indexOfRPort = findParameter(RPORT);
            }

            final Buffer rportBuffer = rport == -1 ? null : Buffers.wrap(rport);

            if (this.indexOfRPort == -1) {
                this.indexOfRPort = this.params.size();
                this.params.add(new Buffer[] { RPORT, rportBuffer });
            } else {
                this.params.get(this.indexOfRPort)[1] = rportBuffer;
            }
            return this;
        }

        public Builder withReceived(final String received) {
            return withReceived(Buffers.wrap(received));
        }

        public Builder withReceived(final Buffer received) {
            if (this.indexOfReceived == -1) {
                this.indexOfReceived = findParameter(RECEIVED);
            }
            if (this.indexOfReceived == -1) {
                this.indexOfReceived = this.params.size();
                this.params.add(new Buffer[] { RECEIVED, received });
            } else {
                this.params.get(this.indexOfReceived)[1] = received;
            }
            return this;
        }

        public Builder withTransportUDP() {
            this.transport = Transport.udp;
            return this;
        }

        public Builder withTransportSCTP() {
            this.transport = Transport.sctp;
            return this;
        }

        public Builder withTransportTCP() {
            this.transport = Transport.tcp;
            return this;
        }

        public Builder withTransportTLS() {
            this.transport = Transport.tls;
            return this;
        }

        public Builder withTransportWS() {
            this.transport = Transport.ws;
            return this;
        }

        public Builder withTransportWSS() {
            this.transport = Transport.wss;
            return this;
        }

        /**
         * Set the transport. Normally, you should really use the {@link #transportUDP()} methods
         * rather than this.
         *
         * @param transport
         * @return
         * @throws SipParseException in case the transport is not any of UDP, TCP, TLS, SCTP or WS.
         */
        public Builder withTransport(final Buffer transport) throws SipParseException {
            this.transport = Transport.of(transport);
            return this;
        }

        public Builder withTransport(final Transport transport) throws SipParseException {
            assertNotNull(transport, "Illegal Transport - transport cannot be null");
            this.transport = transport;
            return this;
        }

        public Builder withTransport(final String transport) throws SipParseException {
            this.transport = Transport.of(transport);
            return this;
        }

        @Override
        public SipHeader.Builder<ViaHeader> withValue(final Buffer value) {
            // TODO: implement me...
            throw new RuntimeException("TODO: not implemented yet");
        }

        @Override
        public ViaHeader build() throws SipParseException {
            if (indexOfBranch == -1) {
                throw new SipParseException("You must specify a branch parameter");
            }

            if (transport == null) {
                transport = Transport.udp;
            }

            if (host == null) {
                throw new SipParseException("You must specify the host of the Via-header");
            }

            final Buffer via = Buffers.createBuffer(1024);
            transferValue(via);

            return new ViaHeaderImpl(via, transport, host, port, params, indexOfBranch, indexOfReceived, indexOfRPort);
        }

        private void transferValue(final Buffer dst) {
            SipParser.SIP2_0_SLASH.getBytes(0, dst);
            this.transport.toUpperCaseBuffer().getBytes(0, dst);
            dst.write(SipParser.SP);
            this.host.getBytes(0, dst);
            if (this.port != -1) {
                dst.write(SipParser.COLON);
                dst.writeAsString(this.port);
            }

            for (final Buffer[] param : this.params) {
                dst.write(SipParser.SEMI);
                param[0].getBytes(0, dst);
                if (param[1] != null) {
                    dst.write(SipParser.EQ);
                    param[1].getBytes(0, dst);
                }
            }
        }

    }
}
