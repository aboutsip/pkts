/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.packet.SipResponse;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipResponseTest extends YajTestBase {

    private SipResponse response;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final Buffer line = this.sipFrameBuffer180Response.readLine();
        final Buffer headers = this.sipFrameBuffer180Response.slice();
        final Buffer payload = null;

        final SipInitialLine initialLine = SipInitialLine.parse(line);
        assertThat(initialLine.isResponseLine(), is(true));
        this.response = new SipResponseImpl((SipResponseLine) initialLine, headers, payload);
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
