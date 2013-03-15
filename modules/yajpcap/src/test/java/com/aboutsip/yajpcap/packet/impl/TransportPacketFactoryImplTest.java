/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.PacketFactory;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.TransportPacketFactory;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class TransportPacketFactoryImplTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Create and verify a simple UDP packet.
     */
    @Test
    public void testCreateUDPPacket() throws Exception {
        final TransportPacketFactory factory = PacketFactory.getInstance().getTransportFactory();
        final Buffer payload = Buffers.wrap("hello world");
        final TransportPacket pkt = factory.create(Protocol.UDP, "10.36.10.10", 9999, "192.168.0.10", 1234, payload);
        assertThat(pkt, not((TransportPacket) null));
        assertThat(pkt.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(pkt.getSourceIP(), is("10.36.10.10"));
        assertThat(pkt.getSourcePort(), is(9999));
        assertThat(pkt.getDestinationIP(), is("192.168.0.10"));
        assertThat(pkt.getDestinationPort(), is(1234));
        assertThat(pkt.getDestinationMacAddress(), is("00:00:00:00:00:00"));

        // change stuff
        pkt.setSourceMacAddress("12:13:14:15:16:17");
        assertThat(pkt.getSourceMacAddress(), is("12:13:14:15:16:17"));

        pkt.setDestinationMacAddress("01:02:03:04:05:06");
        assertThat(pkt.getDestinationMacAddress(), is("01:02:03:04:05:06"));

        pkt.setDestinationIP(10, 20, 30, 40);
        assertThat(pkt.getDestinationIP(), is("10.20.30.40"));

        pkt.setDestinationIP("50.60.70.80");
        assertThat(pkt.getDestinationIP(), is("50.60.70.80"));

        pkt.setSourceIP(11, 22, 33, 44);
        assertThat(pkt.getSourceIP(), is("11.22.33.44"));

        pkt.setSourceIP("55.66.77.88");
        assertThat(pkt.getSourceIP(), is("55.66.77.88"));
    }

}
