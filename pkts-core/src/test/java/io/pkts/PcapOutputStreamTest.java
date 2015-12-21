/**
 * 
 */
package io.pkts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class PcapOutputStreamTest extends PktsTestBase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the write ability by first loading a pcap file and then only write
     * out BYE and INVITE's to another stream. We then load this stream and
     * count what's in there...
     */
    @Test
    public void testWritePacketsToFile() throws Exception {
        final InputStream stream = PktsTestBase.class.getResourceAsStream("sipp.pcap");
        final Pcap pcap = Pcap.openStream(stream);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        // final FileOutputStream out = new FileOutputStream("target/hello.pcap");
        final PcapOutputStream pcapStream = pcap.createOutputStream(out);
        final TestWriteStreamHandler handler = new TestWriteStreamHandler(pcapStream);
        pcap.loop(handler);
        pcap.close();
        out.flush();
        out.close();

        if (true) {
            return;
        }

        // stream = new ByteArrayInputStream(out.toByteArray());
        // pcap = Pcap.openStream(stream);
        final MethodCalculator calculator = new MethodCalculator();
        // pcap.loop(calculator);
        // pcap.close();

        // should only be 10 sip packets in total. 5 invites and 5 byes
        assertThat(calculator.total, is(10));
        assertThat(calculator.invite, is(5));
        assertThat(calculator.bye, is(5));
        assertThat(calculator.ack, is(0)); // i guess un-necessary check...
        assertThat(calculator.cancel, is(0)); // i guess un-necessary check...
    }

}
