/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class PcapRecordHeaderTest {

    @Test
    public void testCreateDefaultHeader() {
        // Sept 5th, 2013 @ 21.52:05.356 - sitting on a plane to Sweden :-)
        final long ts = 1378443125356L;
        final PcapRecordHeader header = PcapRecordHeader.createDefaultHeader(ts);
        assertThat(header.getTimeStampSeconds(), is(ts / 1000));
        assertThat(header.getTimeStampMicroOrNanoSeconds(), is(ts % 1000 * 1000));

        assertThat(header.getCapturedLength(), is(0L));
        assertThat(header.getTotalLength(), is(0L));

        header.setCapturedLength(123456);
        assertThat(header.getCapturedLength(), is(123456L));
        assertThat(header.getTotalLength(), is(0L));

        header.setTotalLength(555);
        assertThat(header.getTotalLength(), is(555L));
        assertThat(header.getCapturedLength(), is(123456L));
    }

}
