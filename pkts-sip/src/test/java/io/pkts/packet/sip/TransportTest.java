package io.pkts.packet.sip;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class TransportTest {

    /**
     * Doesn't really test much but just ensures that no simple copy-paste
     * bugs or accidental changes creeps in.
     */
    @Test
    public void testIsReliable() {
        assertThat(Transport.udp.isReliable(), is(false));
        assertThat(Transport.tcp.isReliable(), is(true));
        assertThat(Transport.tls.isReliable(), is(true));
        assertThat(Transport.ws.isReliable(), is(true));
        assertThat(Transport.wss.isReliable(), is(true));
        assertThat(Transport.sctp.isReliable(), is(true));
    }
}
