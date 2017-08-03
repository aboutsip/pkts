package io.pkts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import io.pkts.packet.Packet;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PcapTest extends PktsTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testLoop() throws Exception {
        // there are 30 packets in this capture.
        final InputStream stream = PktsTestBase.class.getResourceAsStream("sipp.pcap");
        final Pcap pcap = Pcap.openStream(stream);
        final FrameHandlerImpl handler = new FrameHandlerImpl();
        pcap.loop(handler);
        pcap.close();
        assertThat(handler.count, is(30));
    }

    @Test
    public void testWritesPackets() throws Exception {
        final Pcap pcap = Pcap.openStream(PktsTestBase.class.getResourceAsStream("sipp.pcap"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PcapOutputStream pcapOutputStream = pcap.createOutputStream(outputStream);
        pcap.loop(packet -> {
            Packet ipPacket = packet.getPacket(Protocol.IPv4);
            pcapOutputStream.write(ipPacket);
            return true;
        });
        outputStream.flush();

        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        final InputStream resourceStream = PktsTestBase.class.getResourceAsStream("sipp.pcap");
        int bytesRead;
        byte[] data = new byte[resourceStream.available()];
        while ((bytesRead = resourceStream.read(data, 0, data.length)) != -1) {
            expectedStream.write(data, 0, bytesRead);
        }
        expectedStream.flush();

        assertArrayEquals(expectedStream.toByteArray(), outputStream.toByteArray());
    }

    private static class FrameHandlerImpl implements PacketHandler {
        public int count;

        @Override
        public boolean nextPacket(final Packet packet) {
            try {
                final SipPacket sip = (SipPacket) packet.getPacket(Protocol.SIP);
                ++this.count;
            } catch (final Exception e) {
                e.printStackTrace();
                fail("ooops");
            }

            return true;
        }
    }

}
