/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.address.SipURI;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipURIImpl extends URIImpl implements SipURI {

    public static final Buffer SCHEME_SIP = Buffers.wrap("sip");

    public static final Buffer SCHEME_SIPS = Buffers.wrap("sips");

    /**
     * The full raw sip(s) URI
     */
    private final Buffer buffer;

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
    private final Buffer port;

    /**
     * contains the uri-parameters and/or headers.
     */
    private final Buffer paramsHeaders;

    /**
     * Flag indicating whether this is a sips uri or not.
     */
    private final boolean isSecure;

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
    private SipURIImpl(final boolean isSips, final Buffer userInfo, final Buffer host, final Buffer port,
            final Buffer paramsHeaders,
            final Buffer original) {
        super(isSips ? SCHEME_SIPS : SCHEME_SIP);
        this.isSecure = isSips;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.paramsHeaders = paramsHeaders;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer toBuffer() {
        // Currently, we cannot actually change the URI
        // so we will just return the raw original buffer.
        return this.buffer;
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
        return 0;
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
                host = hostPort.slice(0, index);
                port = hostPort;
            }
        }
        if (host == null) {
            hostPort.setReaderIndex(0);
            host = hostPort;
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

}
