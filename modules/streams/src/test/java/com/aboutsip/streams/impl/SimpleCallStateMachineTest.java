/**
 * 
 */
package com.aboutsip.streams.impl;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.streams.StreamsTestBase;
import com.aboutsip.yajpcap.packet.sip.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SimpleCallStateMachineTest extends StreamsTestBase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Just a very basic call
     */
    @Test
    public void testBasicTransitions() throws Exception {
        final SimpleCallStateMachine fsm = driveTraffic("sipp.pcap");
    }

    private SimpleCallStateMachine driveTraffic(final String resource) throws Exception {
        final List<SipMessage> messages = loadMessages(resource);
        final String callId = messages.get(0).getCallIDHeader().getValue().toString();
        final SimpleCallStateMachine fsm = new SimpleCallStateMachine(callId);
        for (final SipMessage msg : messages) {
            fsm.onEvent(msg);
        }
        return fsm;
    }

}
