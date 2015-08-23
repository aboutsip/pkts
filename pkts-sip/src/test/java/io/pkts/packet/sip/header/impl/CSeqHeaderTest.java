/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.CSeqHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class CSeqHeaderTest {

    @Test
    public void testBuildSCseq() {
        CSeqHeader cseq = CSeqHeader.withMethod(Buffers.wrap("INVITE")).build();
        assertThat(cseq.toString(), is("CSeq: 0 INVITE"));

        // we don't actually check the method since it really can be anything.
        cseq = CSeqHeader.withMethod(Buffers.wrap("H")).build();
        assertThat(cseq.toString(), is("CSeq: 0 H"));

        cseq = CSeqHeader.withMethod(Buffers.wrap("ACK")).cseq(102).build();
        assertThat(cseq.toString(), is("CSeq: 102 ACK"));

        final Buffer value = cseq.getValue();
        assertThat(value.toString(), is("102 ACK"));

        assertThat(cseq.getSeqNumber(), is(102L));
        assertThat(cseq.getMethod().toString(), is("ACK"));
    }

}
