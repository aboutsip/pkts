/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ToHeader;


/**
 * Really the same as the other {@link ToHeaderImplTest} but here we are using the factory method
 * from the {@link ToHeader} directly instead.
 * 
 * @author jonas@jonasborjesson.com
 */
public class ToHeaderTest extends AddressParameterHeadersTestBase {

    @Override
    public AddressParametersHeader frameHeader(final Buffer buffer) throws SipParseException {
        return (ToHeaderImpl) ToHeader.create(buffer);
    }

}
