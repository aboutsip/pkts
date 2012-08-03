/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.SIPFramer;
import com.aboutsip.yajpcap.packet.SipMessage;
import com.aboutsip.yajpcap.packet.SipRequest;

/**
 * @author jonas
 *
 */
public class SipFrameTest extends YajTestBase {

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

    @Test
    public void testParseSipRequest() throws Exception {
        final SIPFramer framer = new SIPFramer(this.framerManager);
        final SipFrame frame = framer.frame(this.sipFrameBuffer);
        final SipMessage sip = frame.parse();
        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(((SipRequest) sip).getRequestUri().toString(), is("sip:service@127.0.0.1:5090"));

        // just check some random headers
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).getValue().toString(), is("129"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).getValue().toString(), is("1-16732@127.0.1.1"));
    }

    @Test
    public void testParseSipResponse() throws Exception {
        final SIPFramer framer = new SIPFramer(this.framerManager);
        final SipFrame frame = framer.frame(this.sipFrameBuffer180Response);
        final SipMessage sip = frame.parse();
        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).getValue().toString(), is("1-16732@127.0.1.1"));
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).getValue().toString(), is("0"));
    }

}
