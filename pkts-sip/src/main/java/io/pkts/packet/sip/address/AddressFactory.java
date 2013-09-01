/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface AddressFactory {

    /**
     * Create a new {@link SipURI}.
     * 
     * @param user
     *            the user portion of the {@link SipURI}, may be null.
     * @param host
     *            the host portion of the {@link SipURI}.
     * @return
     */
    SipURI createSipURI(Buffer user, Buffer host);

    /**
     * Creates a new {@link Address} object based off the display name and the
     * {@link URI}.
     * 
     * @param displayName
     * @param uri
     * @return
     * @throws IllegalArgumentException
     *             in case the URI is null.
     */
    Address createAddress(Buffer displayName, URI uri) throws IllegalArgumentException;

    /**
     * Creates a new {@link Address} object based off the {@link URI}.
     * 
     * @param uri
     * @return
     * @throws IllegalArgumentException
     */
    Address createAddress(URI uri) throws IllegalArgumentException;
}
