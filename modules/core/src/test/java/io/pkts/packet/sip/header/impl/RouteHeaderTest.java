/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.AddressParametersHeader;
import io.pkts.packet.sip.header.impl.RouteHeaderImpl;

import org.junit.After;
import org.junit.Before;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class RouteHeaderTest extends AddressParameterHeadersTestBase {

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public AddressParametersHeader frameHeader(final Buffer buffer) throws SipParseException {
        return (RouteHeaderImpl) RouteHeaderImpl.frame(buffer);
    }

}
