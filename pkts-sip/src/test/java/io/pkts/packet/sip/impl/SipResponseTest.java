/**
 * 
 */
package io.pkts.packet.sip.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.packet.sip.SipResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipResponseTest extends PktsTestBase {

    private SipResponse response;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.response = (SipResponse) parseMessage(this.sipFrameBuffer180Response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * The method in a response is a little "trickier" than in a request since
     * you will need to grab it from the CSeq header
     */
    @Test
    public void testGetMethod() throws Exception {
        assertThat(this.response.getMethod().toString(), is("INVITE"));
    }

}
