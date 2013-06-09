/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.pkts.frame.PcapGlobalHeader;

import java.nio.ByteOrder;

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

}
