package io.pkts.framer;

import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sctp.SctpChunk;
import io.pkts.packet.sctp.SctpDataChunk;
import io.pkts.packet.sctp.SctpPacket;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SctpFramerTest extends PktsTestBase {

    /**
     * SCTP pcap I generated using iperf3...
     */
    @Test
    public void testFrameSctp() throws Exception {
        final List<SctpPacket> packets = loadSctpPackets("sctp001.pcap");
        assertThat(packets.size(), is(12));
        assertChunk(packets.get(0), SctpChunk.Type.INIT);
        assertChunk(packets.get(1), SctpChunk.Type.INIT_ACK);
        assertChunk(packets.get(2), SctpChunk.Type.COOKIE_ECHO);
        assertChunk(packets.get(3), SctpChunk.Type.COOKIE_ACK);
        assertChunk(packets.get(4), SctpChunk.Type.DATA);
        assertChunk(packets.get(5), SctpChunk.Type.SACK);
        assertChunk(packets.get(6), SctpChunk.Type.DATA);
        assertChunk(packets.get(7), SctpChunk.Type.SACK);
        assertChunk(packets.get(8), SctpChunk.Type.SACK);
        assertChunk(packets.get(9), SctpChunk.Type.SHUTDOWN);
        assertChunk(packets.get(10), SctpChunk.Type.SHUTDOWN_ACK);
        assertChunk(packets.get(11), SctpChunk.Type.SHUTDOWN_COMPLETE);

        assertThat(packets.get(0).getSourcePort(), is(37188));
        assertThat(packets.get(0).getDestinationPort(), is(1234));

        assertThat(packets.get(1).getSourcePort(), is(1234));
        assertThat(packets.get(1).getDestinationPort(), is(37188));
    }

    /**
     * Test to frame SCTP pcaps that I found as examples here
     * https://wiki.wireshark.org/SampleCaptures#Stream_Control_Transmission_Protocol_.28SCTP.29
     *
     * @throws Exception
     */
    @Test
    public void testFrameSctp2() throws Exception {
        assertThat(loadSctpPackets("sctp.cap").size(), is(4));
        assertThat(loadSctpPackets("sctp-addip.cap").size(), is(38));
        assertThat(loadSctpPackets("sctp-test.cap").size(), is(74));

        List<SctpPacket> packets = loadSctpPackets("sctp-www.cap");
        assertThat(packets.size(), is(84));
        assertChunk(packets.get(0), SctpChunk.Type.INIT);
        assertChunk(packets.get(1), SctpChunk.Type.INIT_ACK);
        assertChunk(packets.get(2), SctpChunk.Type.COOKIE_ECHO);
        assertChunk(packets.get(3), SctpChunk.Type.COOKIE_ACK);
        assertChunk(packets.get(4), SctpChunk.Type.DATA);
        assertChunk(packets.get(5), SctpChunk.Type.SACK);
        assertChunk(packets.get(6), SctpChunk.Type.DATA);
        assertChunk(packets.get(7), SctpChunk.Type.SACK);
        assertChunk(packets.get(8), SctpChunk.Type.DATA);
        assertChunk(packets.get(9), SctpChunk.Type.DATA);
        assertChunk(packets.get(10), SctpChunk.Type.SACK);
        // ...
        assertChunk(packets.get(48), SctpChunk.Type.DATA);
        // ...
        assertChunk(packets.get(65), SctpChunk.Type.SACK);
        // ...
        assertChunk(packets.get(78), SctpChunk.Type.SHUTDOWN);
        assertChunk(packets.get(79), SctpChunk.Type.SHUTDOWN);
        assertChunk(packets.get(80), SctpChunk.Type.SHUTDOWN_ACK);
        assertChunk(packets.get(81), SctpChunk.Type.SHUTDOWN_COMPLETE);
        assertChunk(packets.get(82), SctpChunk.Type.SHUTDOWN_ACK);
        assertChunk(packets.get(83), SctpChunk.Type.SHUTDOWN_COMPLETE);

        packets = loadSctpPackets("sctp_init_collision.cap");
        assertThat(packets.size(), is(34));
        assertChunk(packets.get(0), SctpChunk.Type.INIT);
        assertChunk(packets.get(1), SctpChunk.Type.ABORT);
        assertChunk(packets.get(2), SctpChunk.Type.INIT);
        assertChunk(packets.get(3), SctpChunk.Type.ABORT);
        assertChunk(packets.get(4), SctpChunk.Type.INIT);
        assertChunk(packets.get(5), SctpChunk.Type.ABORT);
        // ...
        assertChunk(packets.get(33), SctpChunk.Type.SHUTDOWN_COMPLETE);
    }


    @Test
    public void testFrameDataChunk() throws Exception {
        final SctpDataChunk chunk = (SctpDataChunk) loadSctpPackets("sctp001.pcap").get(4).getChunks().get(0);
        assertThat(chunk.isImmediate(), is(false));
        assertThat(chunk.isUnordered(), is(false));
        assertThat(chunk.isBeginningFragment(), is(true));
        assertThat(chunk.isEndingFragment(), is(true));

        assertThat(chunk.getStreamIdentifier(), is(0));
        assertThat(chunk.getStreamSequenceNumber(), is(0));
        assertThat(chunk.getPayloadProtocolIdentifier(), is(0L));
        assertThat(chunk.getTransmissionSequenceNumber(), is(2697344624L));

        // This user data is just some garbage that iperf3 generated when I was generating
        // sctp traffic that could be used for these unit tests. Note that the last byte is
        // set to 0x00 so that's why I remove the last byte in the below check...
        //
        // All verified using wireshark.
        final String actual = chunk.getUserData().slice(36).toString();
        assertThat(actual, is("emo6hzilbjqwdson5d5vdmvry5petkl6w5cu"));
        assertThat(chunk.getUserData().getByte(36), is((byte) 0x00));

        // 37 according to wireshark...
        assertThat(chunk.getUserData().capacity(), is(37));
    }

    @Test
    public void testFrameDataChunk2() throws Exception {
        assertDataChunk(loadSctpDataChunk("sctp_init_collision.cap", 24), 3096428048L, 1, "hello world from 192.168.10.105");
        assertDataChunk(loadSctpDataChunk("sctp_init_collision.cap", 25), 3429330720L, 1, "hello world from 192.168.10.104");

        // data contains html so too big/annoying to copy/paste in here. Just checking length instead
        // All the below packets is one big HTML file that has been fragmented across these 6 packets
        assertDataChunk(loadSctpDataChunk("sctp-www.cap", 8), 1677732375L, 0, 1448);
        assertDataChunk(loadSctpDataChunk("sctp-www.cap", 9), 1677732376L, 0, 1448);
        assertDataChunk(loadSctpDataChunk("sctp-www.cap", 11), 1677732377L, 0, 1448);
        assertDataChunk(loadSctpDataChunk("sctp-www.cap", 12), 1677732378L, 0, 1448);
        assertDataChunk(loadSctpDataChunk("sctp-www.cap", 14), 1677732379L, 0, 1448);
        assertDataChunk(loadSctpDataChunk("sctp-www.cap", 15), 1677732380L, 2, 342);

        assertDataChunkFlags(loadSctpDataChunk("sctp-www.cap", 8), false, false, true, false);
        assertDataChunkFlags(loadSctpDataChunk("sctp-www.cap", 9), false, false, false, false);
        assertDataChunkFlags(loadSctpDataChunk("sctp-www.cap", 11), false, false, false, false);
        assertDataChunkFlags(loadSctpDataChunk("sctp-www.cap", 12), false, false, false, false);
        assertDataChunkFlags(loadSctpDataChunk("sctp-www.cap", 14), false, false, false, false);
        assertDataChunkFlags(loadSctpDataChunk("sctp-www.cap", 15), false, false, false, true);
    }

    private void assertDataChunk(final SctpDataChunk chunk, final long expectedTsn, final int expectedPadding, final String expectedPayload) {
        assertDataChunk(chunk, expectedTsn, expectedPadding, Buffers.wrap(expectedPayload));
    }

    private void assertDataChunkFlags(final SctpDataChunk chunk, final boolean iBit, final boolean uBit, final boolean bBit, final boolean eBit) {
        assertThat(chunk.isImmediate(), is(iBit));
        assertThat(chunk.isUnordered(), is(uBit));
        assertThat(chunk.isBeginningFragment(), is(bBit));
        assertThat(chunk.isEndingFragment(), is(eBit));
    }

    private void assertDataChunk(final SctpDataChunk chunk, final long expectedTsn, final int expectedPadding, final Buffer expectedPayload) {
        assertThat(chunk.getUserData(), is(expectedPayload));
        assertDataChunk(chunk, expectedTsn, expectedPadding, expectedPayload.capacity());
    }

    private void assertDataChunk(final SctpDataChunk chunk, final long expectedTsn, final int expectedPadding, final int expectedLength) {
        assertThat(chunk.getTransmissionSequenceNumber(), is(expectedTsn));
        assertThat(chunk.getPadding(), is(expectedPadding));
    }

    /**
     * Assert the chunk type of given {@link SctpPacket}. We do assume there is only a single chunk within each packet.
     */
    private void assertChunk(final SctpPacket pkt, final SctpChunk.Type expectedType) {
        assertThat(pkt.getChunks().size(), is(1));
        assertThat(pkt.getChunks().get(0).getType(), is(expectedType));
    }
}
