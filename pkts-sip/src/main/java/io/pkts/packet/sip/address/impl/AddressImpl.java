/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.URI;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.ifNull;

/**
 * @author jonas@jonasborjesson.com
 */
public final class AddressImpl implements Address {

    /**
     * The full raw address as a buffer. Since an Address is immutable, we keep
     * the original buffer around since it will be used for writing ourselves out
     * to e.g. a stream etc.
     */
    private final Buffer rawAddress;

    /**
     * The display name or empty if it wasn't set.
     */
    private final Buffer displayName;

    /**
     *
     */
    private final URI uri;


    public AddressImpl(final Buffer original, final Buffer displayName, final URI uri) {
        this.rawAddress = original;
        this.displayName = ifNull(displayName, Buffers.EMPTY_BUFFER);
        this.uri = uri;
    }

    public Builder copy() {
        final Builder builder = Address.withURI(uri);
        builder.withDisplayName(displayName);
        return builder;
    }

    /**
     * @return
     */
    @Override
    public Buffer getDisplayName() {
        return this.displayName;
    }

    /**
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @throws SipParseException
     */
    @Override
    public URI getURI() throws SipParseException {
        return this.uri;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer toBuffer() {
        // TODO: once the Buffers are also immutable we do not need to do this stuff anymore.
        return rawAddress.clone();
    }

    @Override
    public String toString() {
        return rawAddress.toString();
    }

    @Override
    public void getBytes(final Buffer dst) {
        this.rawAddress.getBytes(0, dst);
    }
}
