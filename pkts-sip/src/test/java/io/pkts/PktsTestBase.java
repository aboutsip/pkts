/**
 *
 */
package io.pkts;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.Transport;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.impl.SipParser;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
        ethernetFrameBuffer = Buffers.wrap(RawData.rawEthernetFrame);
        sipFrameBuffer = ethernetFrameBuffer.slice(42, ethernetFrameBuffer.capacity());
        final Buffer ethernetFrame = Buffers.wrap(RawData.rawEthernetFrame2);
        sipFrameBuffer180Response = ethernetFrame.slice(42, ethernetFrame.capacity());
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    public SipMessage loadSipMessage(final String resource) throws Exception {
        final Path path = Paths.get(PktsTestBase.class.getResource(resource).toURI());
        final String content = Files.readAllLines(path).stream().collect(Collectors.joining("\r\n"));
        return SipMessage.frame(content);
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
     * Helper method for verifying parts of the {@link ViaHeader}
     *
     * @param via
     * @param host
     * @param transport
     * @param port
     */
    protected void assertViaHeader(final ViaHeader via,
                                   final String host,
                                   final String transport,
                                   final int port) {
        assertThat(via.getHost().toString(), is(host));
        assertThat(via.getTransport().toString(), is(transport.toUpperCase()));
        assertThat(via.getPort(), is(port));
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
            assertThat(uri.getUser().isPresent(), is(false));
        } else {
            assertThat(uri.getUser().orElse(null), is(Buffers.wrap(user)));
        }

        if (host != null) {
            assertThat(uri.getHost().toString(), is(host));
        }

        final Optional<Transport> actualTransport = uri.getTransportParam();
        if (transport == null) {
            assertThat(actualTransport.isPresent(), is(false));
        } else {
            assertThat(actualTransport.get(), is(Transport.of(transport)));
        }

        assertThat(uri.getPort(), is(port));

    }

    protected void assertAddressHeader(final AddressParametersHeader header, final String user, final String host) {
        assertThat("The address header cannot be null", header, notNullValue());
        final SipURI uri = header.getAddress().getURI().toSipURI();
        assertThat(uri.getUser().get().toString(), is(user));
        assertThat(uri.getHost().toString(), is(host));
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

    protected void assertReasonPhrase(final int statusCode, final String expectedReason) throws Exception {
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

    /**
     * Helper method for ensuring that we parse SIP Uri's correctly
     *
     * @param toParse
     * @param expectedUser
     * @param expectedHost
     * @param expectedPort
     * @throws Exception
     */
    protected void assertSipUri(final String toParse, final String expectedUser, final String expectedHost,
                                final int expectedPort) throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final SipURI uri = SipURI.frame(buffer);
        assertSipUri(uri, expectedUser, expectedHost, expectedPort);
    }

    protected void assertSipUri(final SipURI uri, final String expectedUser, final String expectedHost,
                                final int expectedPort) throws Exception {
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getUser().orElse(Buffers.EMPTY_BUFFER), is(Buffers.wrap(expectedUser)));
        assertThat(uri.getHost().toString(), is(expectedHost));
        assertThat(uri.getPort(), is(expectedPort));
    }


}
