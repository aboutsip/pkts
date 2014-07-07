/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.frame.Frame;
import io.pkts.framer.FramerManager;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;
import io.pkts.streams.FragmentListener;
import io.pkts.streams.SipStatistics;
import io.pkts.streams.Stream;
import io.pkts.streams.StreamHandler;
import io.pkts.streams.StreamId;
import io.pkts.streams.StreamListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default {@link StreamHandler} that will try and figure out if the
 * {@link Frame}s it received belongs to a particular {@link Stream} and if so,
 * it will parse the {@link Frame} into a {@link Packet} and add it to the
 * corresponding {@link Stream}.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class DefaultStreamHandler implements StreamHandler {

    /**
     * Our logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(DefaultStreamHandler.class);

    /**
     * The {@link FramerManager}
     */
    private final FramerManager framerManager;

    /**
     * The handler that deals with SIP.
     */
    private SipStreamHandler sipStreamHandler;

    /**
     * The handler that deals with RTP streams.
     */
    private RtpStreamHandler rtpStreamHandler;

    /**
     * If any IP fragments are detected, then we will consule this listener.
     */
    private FragmentListener fragmentListener;

    /**
     * 
     */
    public DefaultStreamHandler() {
        // should really be injected.
        this.framerManager = FramerManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nextPacket(Packet packet) {

        try {
            if (packet.hasProtocol(Protocol.IPv4)) {
                final IPPacket ip = (IPPacket) packet.getPacket(Protocol.IPv4);
                if (ip.isFragmented()) {
                    packet = handleFragmentation(ip);
                    if (packet == null) {
                        return;
                    }
                }
            }

            if (this.sipStreamHandler != null && packet.hasProtocol(Protocol.SIP)) {
                this.sipStreamHandler.processFrame(packet);
            } else if (this.rtpStreamHandler != null && packet.hasProtocol(Protocol.RTP)) {
                this.rtpStreamHandler.processFrame(packet);
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final PacketParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * To make life easier for the user we will figure out the type of
     * {@link StreamListener} the user passed and based on that setup the
     * correct stream analyzer etc.
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addStreamListener(final StreamListener<? extends Packet> listener) {

        try {
            final Method method = listener.getClass().getMethod("endStream", Stream.class);
            final ParameterizedType parameterizedType = (ParameterizedType) method.getGenericParameterTypes()[0];
            final Type[] parameterArgTypes = parameterizedType.getActualTypeArguments();

            // TODO: could actually be more.
            final Type parameterArgType = parameterArgTypes[0];
            final Class<?> parameterArgClass = (Class<?>) parameterArgType;
            if (parameterArgClass.equals(SipPacket.class)) {
                if (this.sipStreamHandler == null) {
                    this.sipStreamHandler = new SipStreamHandler(this.framerManager);
                }
                this.sipStreamHandler.addListener((StreamListener<SipPacket>) listener);
            } else if (parameterArgClass.equals(RtpPacket.class)) {
                if (this.rtpStreamHandler == null) {
                    this.rtpStreamHandler = new RtpStreamHandler(this.framerManager);
                }
                this.rtpStreamHandler.addListener((StreamListener<RtpPacket>) listener);
            }

        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Unable to figure out the paramterized type", e);
        } catch (final SecurityException e) {
            throw new RuntimeException("Unable to access method information due to security constraints", e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("The startStream method doesn't exist. Signature changed?", e);
        } catch (final ClassCastException e) {
            // means that the user had not parameterized the StreamListener
            // interface, which means that we cannot actually detect streams.
            throw new IllegalArgumentException("The supplied listener has not been parameterized");
        }
    }

    /**
     * Helper method to deal with the {@link FragmentListener} since it
     * technically can throw exceptions and stuff so we just want to catch all
     * and log and move on.
     * 
     * @param ipPacket
     * @return
     */
    private IPPacket handleFragmentation(final IPPacket ipPacket) {
        if (this.fragmentListener == null) {
            return null;
        }
        try {
            return this.fragmentListener.handleFragment(ipPacket);
        } catch (final Throwable t) {
            logger.warn("Exception thrown by FragmentListener when processing the IP frame", t);
        }
        return null;
    }

    /**
     * @param listener
     */
    @Override
    public void setFragmentListener(final FragmentListener listener) {
        this.fragmentListener = listener;
    }

    @Override
    public SipStatistics getSipStatistics() {
        if (this.sipStreamHandler != null) {
            return this.sipStreamHandler.getStatistics();
        }

        return null;
    }

    @Override
    public Map<StreamId, ? extends Stream> getStreams() {
        if (this.sipStreamHandler != null) {
            return this.sipStreamHandler.getStreams();
            // return (Map<StreamId, Stream<? extends Packet>>) this.sipStreamHandler.getStreams();
        }

        return null;
    }

}
