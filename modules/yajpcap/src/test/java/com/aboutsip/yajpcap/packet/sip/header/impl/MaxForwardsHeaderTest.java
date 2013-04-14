package com.aboutsip.yajpcap.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.header.MaxForwardsHeader;

public class MaxForwardsHeaderTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

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

}
