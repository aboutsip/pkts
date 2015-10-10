/**
 * 
 */
package io.pkts;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.impl.SipParser;
import org.junit.After;
import org.junit.Before;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
        return SipParser.frame(buffer);
    }

    public SipMessage parseMessage(final String msg) throws Exception {
        return parseMessage(Buffers.wrap(msg));
    }

    /**
     * Assert the value of the header.
     *
     * @param header
     * @param expectedValue
     */
    protected void assertHeader(final SipHeader header, final String expectedValue) {
        assertThat(header.getValue().toString(), is(expectedValue));
    }

    protected void assertReasonPhrase(int statusCode, String expectedReason) throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        final SipResponse response = msg.createResponse(statusCode).build();
        assertThat(response.getReasonPhrase().toString(), is(expectedReason));
    }

    protected void assertHeaderNotPresent(final List<? extends SipHeader> headers) {
        assertThat("Did not expect that header(s) to be present", headers == null || headers.isEmpty(), is(true));
    }

    protected void assertHeaderNotPresent(final SipHeader optional) {
        assertThat("Did not expect that header to be present", optional, is((SipHeader) null));
    }

    protected void assertHeaderNotPresent(final Optional<SipHeader> optional) {
        assertThat("Did not expect that header to be present", optional.isPresent(), is(false));
    }


}
