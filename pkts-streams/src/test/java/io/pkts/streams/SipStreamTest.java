package io.pkts.streams;

import io.pkts.streams.SipStream.CallState;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SipStreamTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        System.out.println(CallState.INITIAL.ordinal());
        System.out.println(CallState.TRYING.ordinal());
        System.out.println(CallState.RINGING.ordinal());
        System.out.println(CallState.IN_CALL.ordinal());
        System.out.println(CallState.CANCELLED.ordinal());
        System.out.println(CallState.COMPLETED.ordinal());
        System.out.println(CallState.REJECTED.ordinal());
        System.out.println(CallState.FAILED.ordinal());
        System.out.println(CallState.UNKNOWN.ordinal());

        System.out.println(CallState.UNKNOWN.compareTo(CallState.RINGING));

    }

}
