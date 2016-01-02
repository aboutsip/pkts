package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.impl.SipMessageStreamBuilder.Configuration;
import io.pkts.packet.sip.impl.SipMessageStreamBuilder.DefaultConfiguration;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageStreamBuilderTest extends PktsTestBase {

    @Test
    public void testBasicParsing001() throws Exception {

        final Consumer<SipMessage> verifier = msg -> {
            final SipRequest request = msg.toRequest();
            try {
                assertSipUri(request.getRequestUri().toSipURI(), "service", "127.0.0.1", 5060);
            } catch (final Exception e) {
                fail(e.getMessage());
            }

            assertThat(request.getCallIDHeader().getValue().toString(), is("1-10091@127.0.1.1"));
            assertThat(request.getCSeqHeader().getMethod().toString(), is("INVITE"));
            assertThat(request.getCSeqHeader().getSeqNumber(), is(1L));

            assertThat(request.getViaHeaders().size(), is(4));
            assertViaHeader(request.getViaHeader(), "127.0.1.1", "UDP", 5061);
            assertViaHeader(request.getViaHeaders().get(0), "127.0.1.1", "UDP", 5061);
            assertViaHeader(request.getViaHeaders().get(1), "12.13.14.15", "WSS", 443);
            assertViaHeader(request.getViaHeaders().get(2), "192.168.0.100", "TLS", 5061);
            assertThat(request.getViaHeaders().get(3).getValue().toString(), is("SIP/2.0/UDP hello.com;branch=asdf-asdf-123-123-abc;received=68.67.66.65"));
            assertThat(request.getContentLength(), is(129));
        };

        parseIt(RawData.sipInviteFourViaHeaders, verifier);
    }

    @Test
    public void testBasicParsing002() throws Exception {

        final Consumer<SipMessage> verifier = msg -> {
            final SipResponse response = msg.toResponse();

            assertThat(response.getCallIDHeader().getValue().toString(), is("1-10091@127.0.1.1"));
            assertThat(response.getCSeqHeader().getMethod().toString(), is("INVITE"));
            assertThat(response.getCSeqHeader().getSeqNumber(), is(1L));

            assertThat(response.getViaHeaders().size(), is(4));
            assertViaHeader(response.getViaHeader(), "127.0.1.1", "UDP", 5061);
            assertViaHeader(response.getViaHeaders().get(0), "127.0.1.1", "UDP", 5061);
            assertViaHeader(response.getViaHeaders().get(1), "12.13.14.15", "WSS", 443);
            assertViaHeader(response.getViaHeaders().get(2), "192.168.0.100", "TLS", 5061);
            assertThat(response.getViaHeaders().get(3).getValue().toString(), is("SIP/2.0/UDP hello.com;branch=asdf-asdf-123-123-abc;received=68.67.66.65"));
            assertThat(response.getContentLength(), is(129));
        };

        parseIt(RawData.twoHundredOkFourViaOnOneLine , verifier);
    }

    @Test
    public void testBasicParsing003() throws Exception {

        final Consumer<SipMessage> verifier = msg -> {
            final SipRequest request = msg.toRequest();

            assertThat(request.getRequestUri().toSipURI().toString(), is("sip:service@8.8.8.8:5060"));
            assertThat(request.getRouteHeader().getValue().toString(), is("<sip:one@aboutsip.com;transport=udp>"));

            assertThat(request.getCallIDHeader().getValue().toString(), is("1-17354@192.168.8.110"));
            assertThat(request.getCSeqHeader().getMethod().toString(), is("INVITE"));
            assertThat(request.getCSeqHeader().getSeqNumber(), is(1L));

            assertThat(request.getViaHeaders().size(), is(1));
            assertViaHeader(request.getViaHeader(), "192.168.8.110", "UDP", 5060);
            assertThat(request.getContentLength(), is(137));
        };

        final byte[] msg = RawData.sipInviteOneRouteHeader;
        parseIt(msg, verifier);

        // now, actually do the same but add some blank stuff in the beginning of
        // the stream. We should be able to by-pass it.
        final byte[] data = new byte[msg.length + 5];
        data[0] = SipParser.SP;
        data[1] = SipParser.HTAB;
        data[2] = SipParser.HTAB;
        data[3] = SipParser.SP;
        data[4] = SipParser.SP;

        System.arraycopy(msg, 0, data, 5, msg.length);
        parseIt(data, verifier);
    }

    private void parseIt(final byte[] data, final Consumer<SipMessage> verifyFunction) throws Exception {
        System.out.println("This we will receive over the wire ---->");
        System.out.print(new String(data));
        System.out.println("<----");

        final Configuration config = new DefaultConfiguration();
        final SipMessageStreamBuilder builder = new SipMessageStreamBuilder(config);
        // pretend that we are getting data in chunks and we should be able
        // to take any chunk size to just test freaking everything including
        // going beyond the max available size available, which really shouldn't
        // matter since we won't copy it but still...
        for (int chunkSize = 1; chunkSize < data.length + 10; ++chunkSize) {
            // System.out.println("Testing with a chunk size of " + chunkSize);
            final SipMessage msg = frameSipMessage(builder, data, chunkSize);
            verifyFunction.accept(msg);

            // make sure that it comes out the exact same way as the original
            // raw data
            assertThat(new String(data), is(msg.toString()));
        }

    }

    private SipMessage frameSipMessage(final SipMessageStreamBuilder builder, final byte[] data, final int chunkSize) {
        final byte[] array = builder.getArray();
        while (builder.getWriterIndex() < data.length) {
            final int copy = Math.min(chunkSize, data.length - builder.getWriterIndex());
            System.arraycopy(data, builder.getWriterIndex(), array, builder.getWriterIndex(), copy);
            if (builder.processNewData(copy)) {
                return builder.build();
            }
        }

        return null;
    }

}
