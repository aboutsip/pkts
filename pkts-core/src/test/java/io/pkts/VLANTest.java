package io.pkts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VLANTest extends PktsTestBase {

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

    // test QinQ packet
    @Test
    public void testLoop() throws Exception {
        final InputStream stream = PktsTestBase.class.getResourceAsStream("event_waf_152197.pcap");
        final Pcap pcap = Pcap.openStream(stream);
        pcap.loop(packet -> {
            while (true) {
                packet = packet.getNextPacket();
                if (packet == null) {
                    break;
                }
            }
            return true;
        });
        pcap.close();
    }

}
