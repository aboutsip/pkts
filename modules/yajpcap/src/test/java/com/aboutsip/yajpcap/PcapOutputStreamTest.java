/**
 * 
 */
package com.aboutsip.yajpcap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.packet.Packet;

/**
 * @author jonas@jonasborjesson.com
 */
public class PcapOutputStreamTest extends YajTestBase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * {@link Frame}s are easy to write out because they can be written out as
     * we read them in since they don't support any modifications.
     * {@link Packet}s on the other hand can be modified and are therefore
     * written to stream differently so make sure this is working too. In either
     * case, they should both work and from a user perspective there shouldn't
     * be any difference. Test that...
     */
    @Test
    public void testWritePacketsToFile() throws Exception {
        assertWrite(false);
        assertWrite(true);
    }

    private void assertWrite(final boolean writePackets) throws Exception {
        InputStream stream = YajTestBase.class.getResourceAsStream("sipp.pcap");
        Pcap pcap = Pcap.openStream(stream);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PcapOutputStream pcapStream = pcap.createOutputStream(out);
        final TestWriteStreamHandler handler = new TestWriteStreamHandler(pcapStream, writePackets);
        pcap.loop(handler);
        pcap.close();
        out.flush();
        out.close();

        stream = new ByteArrayInputStream(out.toByteArray());
        pcap = Pcap.openStream(stream);
        final MethodCalculator calculator = new MethodCalculator();
        pcap.loop(calculator);
        pcap.close();

        // should only be 10 sip packets in total. 5 invites and 5 byes
        assertThat(calculator.total, is(10));
        assertThat(calculator.invite, is(5));
        assertThat(calculator.bye, is(5));
        assertThat(calculator.ack, is(0)); // i guess un-necessary check...
        assertThat(calculator.cancel, is(0)); // i guess un-necessary check...
    }

}
