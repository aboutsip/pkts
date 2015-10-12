/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.header.impl.RouteHeaderImpl;

/**
 * Source: RFC 3261 section 20.30
 * 
 * <p>
 * The Route header field is used to force routing for a request through the
 * listed set of proxies. Examples of the use of the Route header field are in
 * Section 16.12.1.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 *    Route: &lt;sip:bigbox3.site3.atlanta.com;lr&gt;,
 *           &lt;sip:server10.biloxi.com;lr&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RouteHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Route");

    @Override
    RouteHeader clone();

    /**
     * Frame the value as a {@link RouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static RouteHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer original = buffer.slice();
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new RouteHeaderImpl(original, (Address) result[0], (Buffer) result[1]);
    }

    @Override
    Builder copy();

    @Override
    default boolean isRouteHeader() {
        return true;
    }

    @Override
    default RouteHeader toRouteHeader() {
        return this;
    }

    static Builder withHost(final Buffer host) {
        final Builder b = new Builder();
        b.withHost(host);
        return b;
    }

    static Builder withHost(final String host) {
        return withHost(Buffers.wrap(host));
    }

    static Builder withAddress(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.withAddress(address);
        return builder;
    }

    class Builder extends AddressParametersHeader.Builder<RouteHeader> {

        private Builder(ParametersSupport params) {
            super(NAME, params);
        }

        private Builder() {
            super(NAME);
        }

        @Override
        public RouteHeader internalBuild(final Buffer rawValue, final Address address, final Buffer params) throws SipParseException {
            return new RouteHeaderImpl(rawValue, address, params);
        }
    }

}
