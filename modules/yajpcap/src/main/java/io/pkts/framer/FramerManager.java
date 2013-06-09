/**
 * 
 */
package io.pkts.framer;

import io.pkts.Clock;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.protocol.Protocol;
import io.pkts.protocol.Protocol.Layer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/**
 * FramerFactory
 * 
 * @author jonas@jonasborjesson.com
 */
public final class FramerManager {

    private static final FramerManager instance = new FramerManager();

    /**
     * may seem un-necessary but since there will be very few framers and they
     * are fixed this doesn't really waste any memory and I prefer speed and
     * correctness which this structure will help (it will help to avoid
     * {@link Framer#accept(Buffer)} falsely reporting success if there simply
     * are less framers that get a chance to look at the data.)
     */
    private final Map<Protocol, Framer<?>> framers = new HashMap<Protocol, Framer<?>>();
    private final Map<Protocol, Framer<?>> layer2Framer = new HashMap<Protocol, Framer<?>>();
    private final Map<Protocol, Framer<?>> layer3Framer = new HashMap<Protocol, Framer<?>>();
    private final Map<Protocol, Framer<?>> layer4Framer = new HashMap<Protocol, Framer<?>>();
    private final Map<Protocol, Framer<?>> layer7Framer = new HashMap<Protocol, Framer<?>>();

    /**
     * The current time in the system, which is driven by
     * {@link Pcap#loop(io.pkts.FrameHandler)}.
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
        switch (p.getProtocolLayer()) {
        case LAYER_2:
            this.layer2Framer.put(p, framer);
            break;
        case LAYER_3:
            this.layer3Framer.put(p, framer);
            break;
        case LAYER_4:
            this.layer4Framer.put(p, framer);
            break;
        case LAYER_7:
            this.layer7Framer.put(p, framer);
            break;
        }
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
     * Try and find a framer for the data but only look among the {@link Framer}
     * s within the specified {@link Layer}.
     * 
     * You typically want to call this method as opposed to
     * {@link #getFramer(Buffer)} since it will be faster because it will search
     * a smaller space.
     * 
     * @param layer
     *            the {@link Layer} where we expect that the framer will be
     *            found.
     * @param data
     *            the data to frame.
     * @return a {@link Framer} suitable to frame the data.
     * @throws IOException
     */
    public Framer<?> getFramer(final Protocol.Layer layer, final Buffer data) throws IOException {
        switch (layer) {
        case LAYER_2:
            return findFramer(this.layer2Framer, data);
        case LAYER_3:
            return findFramer(this.layer3Framer, data);
        case LAYER_4:
            return findFramer(this.layer4Framer, data);
        case LAYER_7:
            return findFramer(this.layer7Framer, data);
        default:
            // shouldn't happen since we never
            // actually frame Layer_1 stuff.
            throw new RuntimeException("Don't have framers setup for layer " + layer);
        }
    }

    /**
     * Helper method for finding a framer that accepts the data.
     * 
     * @param framers
     * @param data
     * @return
     * @throws IOException
     */
    private Framer<?> findFramer(final Map<Protocol, Framer<?>> framers, final Buffer data) throws IOException {
        for (final Framer<?> framer : framers.values()) {
            if (framer.accept(data)) {
                return framer;
            }
        }

        // unknown
        return null;
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
        return findFramer(this.framers, data);
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
