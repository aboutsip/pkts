/**
 * 
 */
package io.pkts;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipFramer;
import io.pkts.packet.sip.SipMessage;

import org.junit.After;
import org.junit.Before;

/**
 * @author jonas@jonasborjesson.com
 */
public class PktsTestBase {

    /**
     * A full ethernet frame wrapped in a buffer. We will slice out the other
     * frames out of this one so that individual test cases can use the the raw
     * data with ease. All of the indices have been taken from wireshark
     */
    protected Buffer ethernetFrameBuffer;

    /**
     * A raw sip frame buffer.
     */
    protected Buffer sipFrameBuffer;

    /**
     * A raw sip frame buffer containing a 180 response
     */
    protected Buffer sipFrameBuffer180Response;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.ethernetFrameBuffer = Buffers.wrap(RawData.rawEthernetFrame);
        this.sipFrameBuffer = this.ethernetFrameBuffer.slice(42, this.ethernetFrameBuffer.capacity());
        final Buffer ethernetFrame = Buffers.wrap(RawData.rawEthernetFrame2);
        this.sipFrameBuffer180Response = ethernetFrame.slice(42, ethernetFrame.capacity());
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    public SipMessage parseMessage(final byte[] data) throws Exception {
        final Buffer buffer = Buffers.wrap(data);
        return parseMessage(buffer);
    }

    public SipMessage parseMessage(final Buffer buffer) throws Exception {
        return SipFramer.frame(buffer);
    }

    public SipMessage parseMessage(final String msg) throws Exception {
        return parseMessage(Buffers.wrap(msg));
    }

}
