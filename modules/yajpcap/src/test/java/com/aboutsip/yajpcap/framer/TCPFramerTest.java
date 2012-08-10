/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.RawData;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.frame.TCPFrame;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class TCPFramerTest extends YajTestBase {
    private TCPFramer framer;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new TCPFramer(this.framerManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * A syn tcp packet
     * 
     * @throws Exception
     */
    @Test
    public void testTcpSynPacket() throws Exception {
        final Buffer buf = Buffers.wrap(RawData.tcpSyn);
        final Buffer tcp = buf.slice(34, buf.capacity());
        final TCPFrame frame = (TCPFrame) this.framer.frame(tcp);
        assertThat(frame.getSourcePort(), is(59409));
        assertThat(frame.getDestinationPort(), is(5060));
        assertThat(frame.getHeaderLength(), is(40));
    }

    @Test
    public void testTcpFramer() throws Exception {
        final TCPFrame frame = (TCPFrame) this.framer.frame(this.tcpFrameBuffer);
        assertThat(frame.getSourcePort(), is(5060));
        assertThat(frame.getDestinationPort(), is(59409));
        assertThat(frame.getHeaderLength(), is(32));
    }

}
