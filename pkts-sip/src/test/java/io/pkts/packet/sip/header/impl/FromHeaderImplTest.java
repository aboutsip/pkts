/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.FromHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class FromHeaderImplTest extends AddressParameterHeadersTestBase {

    @Override
    public AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException {
        return (FromHeaderImpl) FromHeader.frame(buffer);
    }


}
