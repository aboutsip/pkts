/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.ToHeader;


/**
 * Really the same as the other {@link ToHeaderImplTest} but here we are using the factory method
 * from the {@link ToHeader} directly instead.
 * 
 * @author jonas@jonasborjesson.com
 */
public class ToHeaderTest extends AddressParameterHeadersTestBase {

    @Override
    public AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException {
        return (ToHeaderImpl) ToHeader.frame(buffer);
    }

    @Override
    public AddressParametersHeader.Builder withHost(final String host) {
        return ToHeader.withHost(host);
    }

}
