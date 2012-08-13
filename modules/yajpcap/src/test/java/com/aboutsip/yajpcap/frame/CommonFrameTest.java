/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.frame.layer1.Layer1Frame;
import com.aboutsip.yajpcap.frame.layer4.UDPFrame;
import com.aboutsip.yajpcap.frame.layer7.SDPFrame;
import com.aboutsip.yajpcap.framer.layer2.EthernetFramer;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * This class contains a set of test cases that relate to the the common
 * functionality of all frames.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class CommonFrameTest extends YajTestBase {

    /**
     * A default frame, which contains a full capture of Ethernet II -> IPv4 ->
     * UDP -> SIP -> SDP
     */
    private Frame defaultFrame;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final Layer1Frame parent = mock(Layer1Frame.class);
        final EthernetFramer framer = new EthernetFramer(this.framerManager);
        this.defaultFrame = framer.frame(parent, this.ethernetFrameBuffer);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * It should be possible to ask a frame what other protocols it contains.
     * This will cause the frame to walk down and frame all of its sub-frames
     * until it finds (or not) what it is asked for
     * 
     */
    @Test
    public void testHasProtocol() throws Exception {
        assertThat(this.defaultFrame.hasProtocol(Protocol.ETHERNET_II), is(true));

        // this will cause us to unwind the stack to find SIP
        assertThat(this.defaultFrame.hasProtocol(Protocol.SIP), is(true));

        // which means that we should of course have IP
        assertThat(this.defaultFrame.hasProtocol(Protocol.IPv4), is(true));

        // as well as UDP
        assertThat(this.defaultFrame.hasProtocol(Protocol.UDP), is(true));

        // the SDP still hasn't been framed but when asked for it
        // we should be getting it
        assertThat(this.defaultFrame.hasProtocol(Protocol.SDP), is(true));
    }

    /**
     * Essentially the same as {@link #testHasProtocol()} but since the
     * implementation could be different (right now it isn't) we should still
     * test it in case it ever changes
     * 
     * @throws Exception
     */
    @Test
    public void testGetProtofolFrame() throws Exception {
        // the SDP is at the very end so all the frames
        // has to be traversed. Since this test is not about
        // testing the actual SDP, just do a very basic test
        // so that the data within the sdp frame actually
        // seems to be an SDP
        final SDPFrame f = (SDPFrame) this.defaultFrame.getFrame(Protocol.SDP);
        assertThat(f.getProtocol(), is(Protocol.SDP));
        final String sdp = f.getRawSDP().toString();
        assertTrue(sdp.contains("m=audio 6001 RTP/AVP 0"));

        final UDPFrame udpFrame = (UDPFrame) this.defaultFrame.getFrame(Protocol.UDP);
        assertThat(udpFrame.getSourcePort(), is(5060));
    }

}
