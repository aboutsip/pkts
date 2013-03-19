/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.packet.IPPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public class IPPacketImplTest extends YajTestBase {

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
