/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface URI {

    /**
     * Returns the scheme of this URI, which really can be anything (see RFC3261
     * section 25.1 and the definition of absoluteURI) but most commonly will be
     * "sip", "sips" or "tel".
     * 
     * @return
     */
    Buffer getScheme();

    /**
     * Check whether this {@link URI} is a "sip" or "sips" URI.
     * 
     * @return true if this {@link URI} is a SIP URI, false otherwise.
     */
    default boolean isSipURI() {
    	return false;
    }
    
    /**
     * Check whether this {@link URI} is a "tel" URI.
     * 
     * @return true if this {@link URI} is a TEL URI, false otherwise.
     */
    default boolean isTelURI() {
    	return false;
    }

    /**
     * Write the bytes of this URI into the destination buffer
     * 
     * @param dst
     */
    void getBytes(Buffer dst);

    default SipURI toSipURI() {
        throw new ClassCastException("Unable to cast " + this.getClass().getName() + " into a " + SipURI.class.getName());
    }
    
    default TelURI toTelURI() {
        throw new ClassCastException("Unable to cast " + this.getClass().getName() + " into a " + TelURI.class.getName());
    }


    /**
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    static URI frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        buffer.markReaderIndex();
        final Buffer b = buffer.readBytes(3);
        buffer.resetReaderIndex();
        // not fool proof but when we parse for real we will make sure
        // that it is correct. This is good enough for us.
        if (b.getByte(0) == 's' && b.getByte(1) == 'i' && b.getByte(2) == 'p') {
            return SipURI.frame(buffer);
        } else if (b.getByte(0) == 't' && b.getByte(1) == 'e' && b.getByte(2) == 'l') {
        	 return TelURI.frame(buffer);
        }
        throw new RuntimeException("Have only implemented SIP and TEL uri parsing right now. Sorry");
    }
    
    /**
     * Get the entire content of this {@link URI} as a {@link Buffer}.
     * 
     * @return
     */
    Buffer toBuffer();

    URI clone();

    /**
     * All URIs are immutable so if you wish to change it you need to
     * obtain a copy of it which will return a new builder that allows
     * you to change and build a new URI.
     *
     * @return
     */
    Builder<? extends URI> copy();

    class Builder<T extends URI> {

        T build() {
            return null;
        }

    }


}
