/**
 * 
 */
package com.aboutsip.streams;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.FrameHandler;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.protocol.Protocol;

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
    protected List<SipMessage> loadMessages(final String resource) throws Exception {
        final InputStream stream = StreamsTestBase.class.getResourceAsStream(resource);
        final Pcap pcap = Pcap.openStream(stream);
        final List<SipMessage> messages = new ArrayList<SipMessage>();
        pcap.loop(new FrameHandler() {
            @Override
            public void nextFrame(final Frame frame) {
                try {
                    if (frame.hasProtocol(Protocol.SIP)) {
                        final SipMessage msg = (SipMessage) frame.getFrame(Protocol.SIP).parse();
                        messages.add(msg);
                    }
                } catch (final Exception e) {
                    throw new RuntimeException("Couldn't load the messages from the stream");
                }
            }
        });
        pcap.close();
        return messages;
    }


}
