/**
 * 
 */
package io.pkts;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
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
     * Convenience method for checking {@link AddressParametersHeader}
     *
     * @param host
     * @param port
     */
    protected void assertAddressHeader(final AddressParametersHeader header,
                                       final String displayName,
                                       final String user,
                                       final String host,
                                       final String transport,
                                       final int port) {
        final Address address = header.getAddress();
        final SipURI uri = address.getURI().toSipURI();

        if (displayName == null) {
            assertThat(address.getDisplayName().isEmpty(), is(true));
        } else {
            assertThat(address.getDisplayName().toString(), is(displayName));
        }

        if (user == null) {
            assertThat(uri.getUser().isEmpty(), is(true));
        } else {
            assertThat(uri.getUser().toString(), is(user));
        }

        if (host != null) {
            assertThat(uri.getHost().toString(), is(host));
        }

        final Buffer actualTransport = uri.getTransportParam();
        if (transport == null) {
            assertThat(actualTransport, is((Buffer)null));
        } else {
            assertThat(actualTransport.toString(), is(transport));
        }

        assertThat(uri.getPort(), is(port));

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

    protected void assertHeader(final Optional<SipHeader> header, final String expectedValue) {
        assertThat("Header wasn't present", header.isPresent(), is(true));
        assertThat(header.get().getValue().toString(), is(expectedValue));
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
