/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 *
 */
public class ToHeaderImplTest extends AddressParameterHeadersTestBase {
    @Override
    public AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException {
        return (ToHeaderImpl) ToHeader.frame(buffer);
    }


}
