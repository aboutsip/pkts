package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.ExpiresHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class ExpiresHeaderTest {

    @Test
    public void test() {
        final ExpiresHeader max = new ExpiresHeaderImpl(70);
        assertThat(max.getValue().toString(), is("70"));
        assertThat(max.getExpires(), is(70));
        assertThat(max.toString(), is("Expires: 70"));
        final Buffer copy = Buffers.createBuffer(100);
        max.getBytes(copy);
        assertThat(copy.toString(), is("Expires: 70"));
    }

    @Test
    public void testBuild() {
        final ExpiresHeader m1 = ExpiresHeader.create(600);
        final ExpiresHeader m2 = m1.copy().withValue(890).build();
        final ExpiresHeader m3 = m2.copy().withValue(7000).build();
        assertThat(m1.getExpires(), is(600));
        assertThat(m2.getExpires(), is(890));
        assertThat(m3.getExpires(), is(7000));

        assertThat(m2.toString(), is("Expires: 890"));
        assertThat(m3.toString(), is("Expires: 7000"));

        assertThat(m2.getValue().toString(), is("890"));
        assertThat(m3.getValue().toString(), is("7000"));
    }

}
