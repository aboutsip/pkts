/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aboutsip.streams.FragmentListener;
import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamHandler;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.IPFrame;
import com.aboutsip.yajpcap.frame.IPv4Frame;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.protocol.Protocol;

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
    public void nextFrame(Frame frame) {

        try {
            if (frame.hasProtocol(Protocol.IPv4)) {
                final IPv4Frame ipv4Frame = (IPv4Frame) frame.getFrame(Protocol.IPv4);
                if (ipv4Frame.isFragmented()) {
                    frame = handleFragmentation(ipv4Frame);
                    if (frame == null) {
                        return;
                    }
                }
            }

            if ((this.sipStreamHandler != null) && frame.hasProtocol(Protocol.SIP)) {
                this.sipStreamHandler.processFrame(frame);
            } else if (frame.hasProtocol(Protocol.RTP)) {
                // processRtpFrame((RtpFrame) frame.getFrame(Protocol.RTP));
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
            final Class parameterArgClass = (Class) parameterArgType;
            if (parameterArgClass.equals(SipMessage.class)) {
                if (this.sipStreamHandler == null) {
                    this.sipStreamHandler = new SipStreamHandler(this.framerManager);
                }
                this.sipStreamHandler.addListener((StreamListener<SipMessage>) listener);
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
     * @param frame
     * @return
     */
    private Frame handleFragmentation(final IPFrame frame) {
        if (this.fragmentListener == null) {
            return null;
        }
        try {
            return this.fragmentListener.handleFragment(frame);
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

}
