/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipURIImpl extends URIImpl implements SipURI {

    /**
     * The full raw sip(s) URI
     */
    private Buffer buffer;

    /**
     * The "userinfo" part.
     */
    private final Buffer userInfo;

    /**
     * The host.
     */
    private final Buffer host;

    /**
     * The port
     */
    private Buffer port;

    /**
     * contains the uri-parameters and/or headers.
     */
    private final ParametersSupport paramsSupport;

    /**
     * Flag indicating whether this is a sips uri or not.
     */
    private final boolean isSecure;

    /**
     * Flag telling us whether we need to rebuild the buffer because values have
     * changed.
     */
    private boolean isDirty = false;

    /**
     * 
     * @param isSips
     *            whether this is a sip or sips URL
     * @param userInfo
     *            contains the so-called "userinfo" portion. Typically this is
     *            just the user but can optionally contain a password as well.
     *            See {@link SipParser#consumeUserInfoHostPort(Buffer)} for more
     *            information.
     * @param hostPort
     *            contains the so-called "hostport", which is the domain +
     *            optional port.
     * @param paramsHeaders
     *            any uri-parameters or headers that were on the SIP uri will be
     *            in this buffer. If empty or null then there were none.
     * @param original
     *            the original buffer just because as long as no one is changing
     *            the content we can just return this buffer fast and easy.
     */
    public SipURIImpl(final boolean isSips, final Buffer userInfo, final Buffer host, final Buffer port,
            final Buffer paramsHeaders,
            final Buffer original) {
        super(isSips ? SipParser.SCHEME_SIPS : SipParser.SCHEME_SIP);
        this.isSecure = isSips;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        // note, need to split out the header portion (if there is one)
        this.paramsSupport = new ParametersSupport(paramsHeaders);

        if (original == null) {
            this.isDirty = true;
        }
        this.buffer = original;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSipURI() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public void getBytes(final Buffer dst) {
        if (!this.isDirty) {
            this.buffer.getBytes(dst);
        } else {
            if (this.isSecure) {
                SipParser.SCHEME_SIPS_COLON.getBytes(0, dst);
            } else {
                SipParser.SCHEME_SIP_COLON.getBytes(0, dst);
            }
            if (this.userInfo != null) {
                this.userInfo.getBytes(0, dst);
                dst.write(SipParser.AT);
            }
            this.host.getBytes(0, dst);
            if (this.port != null) {
                dst.write(SipParser.COLON);
                this.port.getBytes(0, dst);
            }

            this.paramsSupport.transferValue(dst);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer toBuffer() {
        if (!this.isDirty) {
            return this.buffer;
        }

        // TODO: need a better strategy around this.
        // Probably want to create a dynamic buffer
        // implementation where this is only the initial size
        final Buffer buffer = Buffers.createBuffer(1024);
        getBytes(buffer);
        this.isDirty = false;
        this.buffer = buffer;
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getUser() {
        // TODO: this is not 100% correct since it may
        // actually contain a password as well.
        if (this.userInfo != null) {
            return this.userInfo.slice();
        }

        return Buffers.EMPTY_BUFFER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getHost() {
        return this.host.slice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        if (this.port == null) {
            return -1;
        }

        try {
            return this.port.parseToInt();
        } catch (final NumberFormatException e) {
            // all of this should already have
            // been checked so should be impossible
            throw new RuntimeException(
                    "The port could not be parsed as an integer. This should not be possible. The port was "
                            + this.port);
        } catch (final IOException e) {
            throw new RuntimeException("IOException while extracting out the port. This should not be possible.");
        }
    }

    @Override
    public void setPort(final int port) {
        this.isDirty = true;
        if (port < 0) {
            this.port = null;
        } else {
            this.port = Buffers.wrap(port);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toBuffer().toString();
    }

    /**
     * Frame a sip or sips-uri, which according to RFC3261 is:
     * 
     * <pre>
     * SIP-URI          =  "sip:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * SIPS-URI         =  "sips:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * </pre>
     * 
     * Remember though that all these frame-functions will only do a basic
     * verification that all things are ok so just because this function return
     * without an exception doesn't mean that you actually framed a valid URI.
     * Everything is done lazily so things may blow up later.
     * 
     * Also note that this function assumes that someone else has already determined the boundaries for this
     * sip(s)-uri and as such, this function do not expect '<' etc.
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static SipURI frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        final Buffer original = buffer.slice();
        final boolean isSips = isSips(buffer);
        final Buffer[] userHost = SipParser.consumeUserInfoHostPort(buffer);
        final Buffer hostPort = userHost[1];
        Buffer host = null;
        Buffer port = null;
        while (hostPort.hasReadableBytes() && host == null) {
            final byte b = hostPort.readByte();
            if (b == SipParser.COLON) {
                final int index = hostPort.getReaderIndex();
                host = hostPort.slice(0, index - 1); // skip the ':'
                port = hostPort;
            }
        }
        if (host == null) {
            hostPort.setReaderIndex(0);
            host = hostPort;
        }

        if (port != null) {
            try {
                port.parseToInt();
            } catch (final NumberFormatException e) {
                throw new SipParseException(0, "The SipURI had a port but it was not an integer: \"" + port.toString()
                        + "\"");
            }
        }
        return new SipURIImpl(isSips, userHost[0], host, port, buffer, original);
    }

    private static boolean isSips(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        SipParser.expect(buffer, 's');
        SipParser.expect(buffer, 'i');
        SipParser.expect(buffer, 'p');
        final byte b = buffer.readByte();
        if (b == SipParser.COLON) {
            return false;
        }

        if (b != 's') {
            throw new SipParseException(buffer.getReaderIndex() - 1,
                    "Expected 's' since the only schemes accepted are \"sip\" and \"sips\"");
        }

        SipParser.expect(buffer, SipParser.COLON);
        return true;
    }

    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException, IllegalArgumentException {
        return this.paramsSupport.getParameter(name);
    }

    @Override
    public Buffer getParameter(final String name) throws SipParseException, IllegalArgumentException {
        return this.paramsSupport.getParameter(name);
    }

    @Override
    public Buffer setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        return this.paramsSupport.setParameter(name, value);
    }

}
