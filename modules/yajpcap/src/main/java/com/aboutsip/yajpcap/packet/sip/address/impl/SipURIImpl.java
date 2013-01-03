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
     * Flag indicating whether this is a sips uri or not.
     */
    private final boolean isSecure;

    /**
     * 
     */
    private SipURIImpl(final boolean isSips, final Buffer buffer) {
        super(isSips ? SCHEME_SIPS : SCHEME_SIP);
        this.buffer = buffer;
        this.isSecure = isSips;
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
        return new SipURIImpl(isSips, original);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getUser() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getHost() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toBuffer().toString();
    }

}
