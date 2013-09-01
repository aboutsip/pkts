/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.AddressParametersHeader;
import io.pkts.packet.sip.header.impl.ToHeaderImpl;

import org.junit.After;
import org.junit.Before;


/**
 * @author jonas@jonasborjesson.com
 */
public class ToHeaderTest extends AddressParameterHeadersTestBase {

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
        return (ToHeaderImpl) ToHeaderImpl.frame(buffer);
    }


}
