package io.pkts.packet.impl;

import io.pkts.PktsTestBase;
import io.pkts.packet.IPv6Packet;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class IPv6PacketImplTest extends PktsTestBase {
    @Test
    public void isFragmented() throws Exception {
        List<Packet> packets = loadStream("ipv6_frag.pcap");
        List<IPv6Packet> ipPackets = packets.stream().map(p -> {
            try {
                return (IPv6Packet) p.getPacket(Protocol.IPv6);
            } catch (IOException e) {
                return null;
            }
        }).collect(Collectors.toList());

        assertTrue(ipPackets.get(0).isFragmented());
        assertEquals(0, ipPackets.get(0).getFragmentOffset());
        assertTrue(ipPackets.get(1).isFragmented());
        assertEquals(1232, ipPackets.get(1).getFragmentOffset());
        assertTrue(ipPackets.get(2).isFragmented());
        assertEquals(2464, ipPackets.get(2).getFragmentOffset());
        assertTrue(ipPackets.get(3).isFragmented());
        assertEquals(3696, ipPackets.get(3).getFragmentOffset());
        assertTrue(ipPackets.get(4).isFragmented());
        assertEquals(4928, ipPackets.get(4).getFragmentOffset());
    }

}