/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import org.junit.After;
import org.junit.Before;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.header.FromHeader;

/**
 * Test the {@link FromHeader}.
 * 
 * @author jonas@jonasborjesson.com
 */
public class FromHeaderTest extends AddressParameterHeadersTestBase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public AddressParametersHeader frameHeader(final Buffer buffer) throws SipParseException {
        return (FromHeaderImpl) FromHeaderImpl.frame(buffer);
    }

}
