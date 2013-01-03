/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.address.URI;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class URIImpl implements URI {

    private final Buffer scheme;

    /**
     * 
     */
    public URIImpl(final Buffer scheme) {
        this.scheme = scheme;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getScheme() {
        return this.scheme;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSipURI() {
        return false;
    }

    /**
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static URI frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        if (isSipURI(buffer)) {
            return SipURIImpl.frame(buffer);
        } else if (isTelURI(buffer)) {
            throw new RuntimeException("Sorry, can't do Tel URIs right now. Haven't implemented it just yet...");
        }
        throw new RuntimeException("Have only implemented SIP uri parsing right now. Sorry");
    }

    private static boolean isSipURI(final Buffer buffer) throws SipParseException {
        buffer.markReaderIndex();
        try {
            final Buffer b = buffer.readBytes(4);
            if ((b.getByte(0) == 's') && (b.getByte(1) == 'i') && (b.getByte(2) == 'p')) {
                final byte last = b.getByte(3);
                if (last == ':') {
                    return true;
                }
                if ((last == 's') && (buffer.peekByte() == ':')) {
                    return true;
                }
            }
        } catch (final IOException e) {
            // ignore
        } finally {
            buffer.resetReaderIndex();
        }

        return false;
    }

    private static boolean isTelURI(final Buffer buffer) throws SipParseException {
        buffer.markReaderIndex();
        try {
            final Buffer b = buffer.readBytes(4);
            if ((b.getByte(0) == 't') && (b.getByte(1) == 'e') && (b.getByte(2) == 'l') && (b.getByte(3) == ':')) {
                return true;
            }
        } catch (final IOException e) {
            // ignore
        } finally {
            buffer.resetReaderIndex();
        }

        return false;
    }

}
