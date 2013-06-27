/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.impl.AddressParametersHeader;
import io.pkts.packet.sip.header.impl.FromHeaderImpl;

import org.junit.After;
import org.junit.Before;


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
