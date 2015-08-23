package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class MaxForwardsHeaderTest {

    @Test
    public void test() {
        final MaxForwardsHeader max = new MaxForwardsHeaderImpl(70);
        assertThat(max.getValue().toString(), is("70"));
        assertThat(max.getMaxForwards(), is(70));
        assertThat(max.toString(), is("Max-Forwards: 70"));
        final Buffer copy = Buffers.createBuffer(100);
        max.getBytes(copy);
        assertThat(copy.toString(), is("Max-Forwards: 70"));
    }

    @Test
    public void testBuild() {
        final MaxForwardsHeader m1 = MaxForwardsHeader.create();
        final MaxForwardsHeader m2 = m1.copy().decrement().build();
        final MaxForwardsHeader m3 = m2.copy().decrement().build();
        assertThat(m1.getMaxForwards(), is(70));
        assertThat(m2.getMaxForwards(), is(69));
        assertThat(m3.getMaxForwards(), is(68));

        assertThat(m2.toString(), is("Max-Forwards: 69"));
        assertThat(m3.toString(), is("Max-Forwards: 68"));

        assertThat(m2.getValue().toString(), is("69"));
        assertThat(m3.getValue().toString(), is("68"));
    }

}
