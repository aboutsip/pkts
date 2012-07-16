package com.aboutsip.yajpcap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.nio.ByteOrder;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.frame.PcapFrame;
import com.aboutsip.yajpcap.frame.PcapGlobalHeader;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.framer.PcapFramer;

/**
 * Test base for all tests regarding framing and parsing
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public class YajTestBase {

    protected FramerManager framerManager;

    /**
     * Default stream pointing to a pcap that contains some sip traffic
     */
    protected Buffer pcapStream;

    protected ByteOrder defaultByteOrder;

    /**
     * Default frame that most tests can use to test their basic framing
     * abilities
     */
    protected Buffer defaultFrame;

    /**
     * The default header for the default pcap stream
     */
    protected PcapGlobalHeader defaultPcapHeader;

    /**
     * The default pcap frame
     */
    protected PcapFrame defaultPcapFrame;

    /**
     * A full ethernet frame wrapped in a buffer. We will slice out the other
     * frames out of this one so that individual test cases can use the the raw
     * data with ease. All of the indices have been taken from wireshark
     */
    protected Buffer ethernetFrameBuffer;

    /**
     * A raw ipv4 frame buffer.
     */
    protected Buffer ipv4FrameBuffer;

    /**
     * A raw udp frame buffer.
     */
    protected Buffer udpFrameBuffer;

    /**
     * A raw sip frame buffer.
     */
    protected Buffer sipFrameBuffer;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.framerManager = FramerManager.getInstance();
        final InputStream stream = YajTestBase.class.getResourceAsStream("sipp.pcap");

        // final InputStream stream = new
        // FileInputStream("/home/jonas/work/GLL/noSound.pcap");
        this.pcapStream = Buffers.wrap(stream);

        // Get the first frame for tests to use. Since this actually will
        // use the PcapFramer to frame it, make sure the PcapFramer isn't
        // broken so do some assertions on it...
        this.defaultPcapHeader = PcapGlobalHeader.parse(this.pcapStream);
        this.defaultByteOrder = this.defaultPcapHeader.getByteOrder();
        final PcapFramer framer = new PcapFramer(this.defaultByteOrder, this.framerManager);
        this.defaultPcapFrame = (PcapFrame) framer.frame(this.pcapStream);
        this.defaultFrame = this.defaultPcapFrame.getPayload();
        assertThat(547, is((this.defaultFrame.capacity())));

        this.ethernetFrameBuffer = Buffers.wrap(RawData.rawEthernetFrame);

        // slice out the individual payloads so that our tests can work
        // directly on this raw data.
        this.ipv4FrameBuffer = this.ethernetFrameBuffer.slice(14, this.ethernetFrameBuffer.capacity());
        this.udpFrameBuffer = this.ethernetFrameBuffer.slice(34, this.ethernetFrameBuffer.capacity());
        this.sipFrameBuffer = this.ethernetFrameBuffer.slice(42, this.ethernetFrameBuffer.capacity());
    }

    @After
    public void tearDown() throws Exception {
    }


}
