/**
 * 
 */
package io.pkts.packet.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.packet.IPPacket;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class IPPacketImplTest extends PktsTestBase {

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testIpLengths() throws Exception {
        final List<IPPacket> ipPackets = loadIPPackets("sipp.pcap");
        assertThat(ipPackets.get(0).getTotalIPLength(), is(533));
        assertThat(ipPackets.get(1).getTotalIPLength(), is(334));
        assertThat(ipPackets.get(2).getTotalIPLength(), is(493));
        assertThat(ipPackets.get(3).getTotalIPLength(), is(384));
        assertThat(ipPackets.get(4).getTotalIPLength(), is(533));
        assertThat(ipPackets.get(5).getTotalIPLength(), is(334));
        assertThat(ipPackets.get(6).getTotalIPLength(), is(493));
        assertThat(ipPackets.get(7).getTotalIPLength(), is(384));
        assertThat(ipPackets.get(8).getTotalIPLength(), is(384));
        assertThat(ipPackets.get(9).getTotalIPLength(), is(326));
        assertThat(ipPackets.get(10).getTotalIPLength(), is(533));
        assertThat(ipPackets.get(11).getTotalIPLength(), is(334));
        assertThat(ipPackets.get(12).getTotalIPLength(), is(493));
        assertThat(ipPackets.get(13).getTotalIPLength(), is(384));
        assertThat(ipPackets.get(14).getTotalIPLength(), is(384));
        assertThat(ipPackets.get(15).getTotalIPLength(), is(326));
        assertThat(ipPackets.get(16).getTotalIPLength(), is(533));
        assertThat(ipPackets.get(17).getTotalIPLength(), is(334));
    }

    @Test
    public void testIpChecksum() throws Exception {
        final List<IPPacket> ipPackets = loadIPPackets("sipp.pcap");
        for (final IPPacket pkt : ipPackets) {
            assertThat(pkt.verifyIpChecksum(), is(true));
        }

        // the following values have been verified
        // through wireshark
        assertThat(ipPackets.get(0).getIpChecksum(), is(Integer.parseInt("3ad6", 16)));
        assertThat(ipPackets.get(1).getIpChecksum(), is(Integer.parseInt("3b9d", 16)));
        assertThat(ipPackets.get(2).getIpChecksum(), is(Integer.parseInt("3afe", 16)));
        assertThat(ipPackets.get(3).getIpChecksum(), is(Integer.parseInt("3b6b", 16)));
        assertThat(ipPackets.get(4).getIpChecksum(), is(Integer.parseInt("3ad6", 16)));
        assertThat(ipPackets.get(5).getIpChecksum(), is(Integer.parseInt("3b9d", 16)));
        assertThat(ipPackets.get(6).getIpChecksum(), is(Integer.parseInt("3afe", 16)));
        assertThat(ipPackets.get(7).getIpChecksum(), is(Integer.parseInt("3b6b", 16)));
    }

}
