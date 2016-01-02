package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.impl.SipMessageStreamBuilder.Configuration;
import io.pkts.packet.sip.impl.SipMessageStreamBuilder.DefaultConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageStreamBuilderTest extends PktsTestBase {

    @Test
    public void testBasicParsing() throws Exception {
        final Configuration config = new DefaultConfiguration();
        final SipMessageStreamBuilder builder = new SipMessageStreamBuilder(config);

        final byte[] data = RawData.sipInviteFourViaHeaders;
        // pretend that we are getting data in chunks and we should be able
        // to take any chunk size to just test freaking everything including
        // going beyond the max available size available, which really shouldn't
        // matter since we won't copy it but still...
        for (int chunkSize = 1; chunkSize < data.length + 10; ++chunkSize) {
            // System.out.println("Testing with a chunk size of " + chunkSize);
            final SipRequest request = frameSipMessage(builder, data, chunkSize).toRequest();
            assertSipUri(request.getRequestUri().toSipURI(), "service", "127.0.0.1", 5060);

            assertThat(request.getViaHeaders().size(), is(4));
            assertViaHeader(request.getViaHeader(), "127.0.1.1", "UDP", 5061);
            assertViaHeader(request.getViaHeaders().get(0), "127.0.1.1", "UDP", 5061);
            assertViaHeader(request.getViaHeaders().get(1), "12.13.14.15", "WSS", 443);
            assertThat(request.getContentLength(), is(129));
            assertThat(new String(data), is(request.toString()));
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
