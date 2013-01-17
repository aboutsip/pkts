/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.Clock;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * FramerFactory
 * 
 * @author jonas@jonasborjesson.com
 */
public final class FramerManager {

    private static final FramerManager instance = new FramerManager();

    private final Map<Protocol, Framer<?>> framers = new HashMap<Protocol, Framer<?>>();

    /**
     * The current time in the system, which is driven by
     * {@link Pcap#loop(com.aboutsip.yajpcap.FrameHandler)}.
     */
    private final PcapClock clock = new PcapClock();

    static {
        // should be moved somewhere else but for now...
        instance.registerDefaultFramers();
    }

    public static final FramerManager getInstance() {
        return instance;
    }

    /**
     * 
     */
    private FramerManager() {
        // left empty intentionally
    }

    public Framer<?> getFramer(final Protocol p) {
        return this.framers.get(p);
    }

    /**
     * Move the {@link Clock} to the specified time.
     * 
     * @param time
     */
    public void tick(final long time) {
        this.clock.tick(time);
    }

    public Clock getClock() {
        return this.clock;
    }

    /**
     * Register a new framer for a particular protocol.
     * 
     * @param p
     * @param framerClass
     * @return
     */
    public void registerFramer(final Protocol p, final Framer framer) throws IllegalArgumentException {
        if ((p == null) || (framer == null)) {
            throw new IllegalArgumentException("The protocol or framer cannot be null");
        }
        this.framers.put(p, framer);
    }

    /**
     * Convenience method for registering all the default framers for all the
     * protocols we currently can handle
     */
    public void registerDefaultFramers() {
        registerFramer(Protocol.SIP, new SIPFramer(this));
        registerFramer(Protocol.SDP, new SDPFramer(this));
        registerFramer(Protocol.SLL, new SllFramer(this));
        registerFramer(Protocol.ETHERNET_II, new EthernetFramer(this));
        registerFramer(Protocol.IPv4, new IPv4Framer(this));
        registerFramer(Protocol.UDP, new UDPFramer(this));
        registerFramer(Protocol.TCP, new TCPFramer(this));
        registerFramer(Protocol.RTP, new RTPFramer(this));
    }

    /**
     * Try and find a framer for the data
     * 
     * @param data the data we are trying to find a framer for
     * @return
     */
    public Framer<?> getFramer(final Buffer data) throws IOException {
        // TODO: if we know which ports the data was sent from
        // and came in on, we should be able to find a framer
        // faster. Hence, allow the user to register known ports
        // for certain protocols. However, this is just an optimization
        // and we would still have to loop over all of them in case
        // the port lookup doesn't turn out to be true

        for (final Framer<?> framer : this.framers.values()) {
            if (framer.accept(data)) {
                return framer;
            }
        }

        // unknown data type
        return null;
    }

    private static class PcapClock implements Clock {

        private final AtomicLong currentTime = new AtomicLong();

        public PcapClock() {
            // left empty intentionally
        }

        @Override
        public long currentTimeMillis() {
            return this.currentTime.get();
        }

        public void tick(final long time) {
            // final SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
            // final Date date = new Date(time / 1000);
            // System.out.println("Time is: " + formatter.format(date));
            this.currentTime.set(time);
        }

    }

}
