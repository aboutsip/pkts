package io.pkts.framer.io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import org.junit.Test;

public class IPv6FramerTest extends PktsTestBase {
    @Test
    public void testIPv6Decode() throws Exception {
        for (Packet p : loadStream("ipv6_http.pcap")) {
            assertThat(p.getNextPacket().getNextPacket().getProtocol(), is(Protocol.IPv6));
            IPPacket ipPacket = (IPPacket) p.getNextPacket().getNextPacket();
            assertThat(ipPacket.getSourceIP(), anyOf(is("2002:17fc:32a0:3:9e5:78a4:e52b:d097"), is("2607:f8b0:400a:800:0:0:0:200e")));
            assertThat(ipPacket.getDestinationIP(), anyOf(is("2002:17fc:32a0:3:9e5:78a4:e52b:d097"), is("2607:f8b0:400a:800:0:0:0:200e")));
            TransportPacket transportPacket = (TransportPacket) ipPacket.getNextPacket();
            assertThat(transportPacket.getProtocol(), is(Protocol.TCP));
            if (transportPacket.getSourcePort() == 80) {
                // Downstream
                assertThat(ipPacket.getIdentification(), is(0x3585));
            } else {
                // Upstream
                assertThat(ipPacket.getIdentification(), is(0x3882));
            }
        }
    }
}
