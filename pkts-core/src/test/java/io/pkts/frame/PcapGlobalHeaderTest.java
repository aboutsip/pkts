/**
 * 
 */
package io.pkts.frame;

import static io.pkts.frame.PcapGlobalHeader.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class PcapGlobalHeaderTest {

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
     * Make sure we can create a correct default {@link PcapGlobalHeader}
     */
    @Test
    public void testCreateDefaultHeader() {
        final PcapGlobalHeader header = PcapGlobalHeader.createDefaultHeader();
        assertThat(header.getMajorVersion(), is(2));
        assertThat(header.getMinorVersion(), is(4));
        assertThat(header.getSnapLength(), is(65535L));
        assertThat(header.getTimeAccuracy(), is(0));
        assertThat(header.getTimeZoneCorrection(), is(0L));
        assertThat(header.getDataLinkType(), is(1));
        assertThat(header.getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));
    }

    private static byte[] getDummyBody() {
        Buffer body = Buffers.createBuffer(20);
        body.setUnsignedByte(0, (short) 2);
        body.setUnsignedByte(2, (short) 4);
        body.setUnsignedInt(4, 0);
        body.setUnsignedInt(8, 0);
        body.setUnsignedInt(12, 65535);
        return body.getRawArray();
    }

    /**
     * Make sure the right magic number is written
     */
    @Test
    public void littleEndianMicrosecondsHeader() throws IOException {
        final byte[] body = getDummyBody();
        PcapGlobalHeader header = new PcapGlobalHeader(ByteOrder.LITTLE_ENDIAN, body, false);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        header.write(os);
        assertArrayEquals(Arrays.copyOf(os.toByteArray(), 4), MAGIC_LITTLE_ENDIAN);
    }

    /**
     * Make sure the right magic number is written
     */
    @Test
    public void bigEndianMicrosecondsHeader() throws IOException {
        final byte[] body = getDummyBody();
        PcapGlobalHeader header = new PcapGlobalHeader(ByteOrder.BIG_ENDIAN, body, false);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        header.write(os);
        assertArrayEquals(Arrays.copyOf(os.toByteArray(), 4), MAGIC_BIG_ENDIAN);
    }

    /**
     * Make sure the right magic number is written
     */
    @Test
    public void littleEndianNanosecondsHeader() throws IOException {
        final byte[] body = getDummyBody();
        PcapGlobalHeader header = new PcapGlobalHeader(ByteOrder.LITTLE_ENDIAN, body, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        header.write(os);
        assertArrayEquals(Arrays.copyOf(os.toByteArray(), 4), MAGIC_NSEC_SWAPPED);
    }

    /**
     * Make sure the right magic number is written
     */
    @Test
    public void bigEndianNanosecondsHeader() throws IOException {
        final byte[] body = getDummyBody();
        PcapGlobalHeader header = new PcapGlobalHeader(ByteOrder.BIG_ENDIAN, body, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        header.write(os);
        assertArrayEquals(Arrays.copyOf(os.toByteArray(), 4), MAGIC_NSEC);
    }
}
