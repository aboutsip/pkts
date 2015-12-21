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

        cseq = CSeqHeader.withMethod(Buffers.wrap("ACK")).withCSeq(102).build();
        assertThat(cseq.toString(), is("CSeq: 102 ACK"));

        final Buffer value = cseq.getValue();
        assertThat(value.toString(), is("102 ACK"));

        assertThat(cseq.getSeqNumber(), is(102L));
        assertThat(cseq.getMethod().toString(), is("ACK"));
    }

    @Test
    public void testCopyConstructor() {
        final CSeqHeader c1 = CSeqHeader.withMethod("INVITE").withCSeq(10).build();
        final CSeqHeader c2 = c1.copy().build();
        final CSeqHeader c3 = c1.copy().withCSeq(11).build();
        final CSeqHeader c4 = c3.copy().withMethod("ACK").build();

        assertThat(c1.toString(), is("CSeq: 10 INVITE"));
        assertThat(c2.toString(), is("CSeq: 10 INVITE"));
        assertThat(c3.toString(), is("CSeq: 11 INVITE"));
        assertThat(c4.toString(), is("CSeq: 11 ACK"));

        assertThat(c1.getValue().toString(), is("10 INVITE"));
        assertThat(c2.getValue().toString(), is("10 INVITE"));
        assertThat(c3.getValue().toString(), is("11 INVITE"));
        assertThat(c4.getValue().toString(), is("11 ACK"));
    }
}
