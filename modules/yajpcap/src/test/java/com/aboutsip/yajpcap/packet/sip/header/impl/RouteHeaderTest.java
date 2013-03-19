/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import org.junit.After;
import org.junit.Before;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

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
