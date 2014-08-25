/**
 * 
 */
package io.pkts.packet.sip.address;

import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import static io.pkts.packet.sip.impl.PreConditions.ifNull;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.impl.AddressImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public final class AddressBuilder {

    private URI uri;
    private Buffer displayName;

    private AddressBuilder() {
        // left empty intentionally
    }

    private AddressBuilder(final Buffer displayName) {
        this.displayName = displayName;
    }

    private AddressBuilder(final URI uri) {
        this.uri = assertNotNull(uri, "URI cannot be null");
    }

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
    public static AddressBuilder with() {
        return new AddressBuilder();
    }

    public static AddressBuilder with(final Buffer displayName) {
        return new AddressBuilder(displayName);
    }

    public static AddressBuilder with(final String displayName) {
        return new AddressBuilder(Buffers.wrap(displayName));
    }

    public static AddressBuilder with(final URI uri) {
        return new AddressBuilder(assertNotNull(uri, "URI cannot be null"));
    }

    public AddressBuilder displayName(final Buffer displayName) {
        this.displayName = ifNull(displayName, Buffers.EMPTY_BUFFER);
        return this;
    }

    public AddressBuilder displayName(final String displayName) {
        this.displayName = Buffers.wrap(ifNull(displayName, ""));
        return this;
    }

    public AddressBuilder uri(final URI uri) {
        this.uri = assertNotNull(uri, "URI cannot be null");
        return this;
    }

    public Address build() throws IllegalArgumentException {
        return new AddressImpl(this.displayName, assertNotNull(this.uri, "URI cannot be null"));
    }

}
