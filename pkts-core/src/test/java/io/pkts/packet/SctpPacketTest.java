package io.pkts.packet;

import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sctp.SctpChunk;
import io.pkts.packet.sctp.SctpPacket;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SctpPacketTest extends PktsTestBase {

    private IPPacket ipPacket;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipPacket = Mockito.mock(IPPacket.class);
    }

    @Test
    public void testFrameSctpInit() throws Exception {
        // this is the SCTP INIT as found in the, also, check in "sctp001.pcap", in case you want to
        // double check.
        final Buffer buffer = loadBuffer("sctpInit.raw");
        final SctpPacket pkt = SctpPacket.frame(ipPacket, buffer);
        assertThat(pkt, notNullValue());
        assertThat(pkt.getChunks().size(), is(1));
        assertThat(pkt.getChunks().get(0).getType(), is(SctpChunk.Type.INIT));

    }

}