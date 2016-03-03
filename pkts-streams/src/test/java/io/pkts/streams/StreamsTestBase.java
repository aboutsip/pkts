/**
 * 
 */
package io.pkts.streams;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.Packet;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author jonas@jonasborjesson.com
 */
public class StreamsTestBase {

    /**
     * Default stream pointing to a pcap that contains some sip traffic
     */
    protected Buffer pcapStream;

    @BeforeClass
    public static void beforeClass() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        final InputStream stream = StreamsTestBase.class.getResourceAsStream("sipp.pcap");
        this.pcapStream = Buffers.wrap(stream);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Convenience method for loading all the SIP messages from a pcap. The pcap
     * MUST be placed in the resource folder under com/aboutsip/streams since
     * this method assumes that.
     * 
     * @param resource
     * @return
     * @throws Exception
     */
    protected List<SipPacket> loadMessages(final String resource) throws Exception {
        final InputStream stream = StreamsTestBase.class.getResourceAsStream(resource);
        final Pcap pcap = Pcap.openStream(stream);
        final List<SipPacket> messages = new ArrayList<SipPacket>();
        pcap.loop(new PacketHandler() {

            @Override
            public boolean nextPacket(final Packet pkt) {
                try {
                    if (pkt.hasProtocol(Protocol.SIP)) {
                        final SipPacket msg = (SipPacket) pkt.getPacket(Protocol.SIP);
                        messages.add(msg);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Couldn't load the messages from the stream");
                }

                return true;
            }
        });
        pcap.close();
        return messages;
    }

}
