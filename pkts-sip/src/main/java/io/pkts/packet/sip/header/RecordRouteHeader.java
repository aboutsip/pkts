/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.header.impl.RecordRouteHeaderImpl;

/**
 * 
 * Source: RFC 3261 section 20.30
 * 
 * <p>
 * The Record-Route header field is inserted by proxies in a request to force
 * future requests in the dialog to be routed through the proxy.
 * </p>
 * 
 * <p>
 * Examples of its use with the Route header field are described in Sections
 * 16.12.1.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 *    Record-Route: &lt;sip:server10.biloxi.com;lr&gt;,
 *                  &lt;sip:bigbox3.site3.atlanta.com;lr&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RecordRouteHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Record-Route");

    @Override
    RecordRouteHeader clone();


    /**
     * Frame the value as a {@link RecordRouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static RecordRouteHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer original = buffer.slice();
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new RecordRouteHeaderImpl(original, (Address) result[0], (Buffer) result[1]);
    }

    @Override
    Builder copy();

    @Override
    default boolean isRecordRouteHeader() {
        return true;
    }

    @Override
    default RecordRouteHeader toRecordRouteHeader() {
        return this;
    }

    static Builder withUser(final Buffer user) {
        final Builder b = new Builder();
        b.withUser(user);
        return b;
    }

    static Builder withUser(final String user) {
        return withUser(Buffers.wrap(user));
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

    class Builder extends AddressParametersHeader.Builder<RecordRouteHeader> {

        private Builder(ParametersSupport params) {
            super(NAME, params);
        }

        private Builder() {
            super(NAME);
        }

        @Override
        public RecordRouteHeader internalBuild(final Buffer rawValue, final Address address, final Buffer params) throws SipParseException {
            return new RecordRouteHeaderImpl(rawValue, address, params);
        }
    }

}
