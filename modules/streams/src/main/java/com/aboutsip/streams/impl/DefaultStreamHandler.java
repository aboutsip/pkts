/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamHandler;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.yajpcap.frame.Frame;
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

    private SipStreamHandler sipStreamHandler;

    /**
     * 
     */
    public DefaultStreamHandler() {
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nextFrame(final Frame frame) {

        try {
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
                    this.sipStreamHandler = new SipStreamHandler();
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

}
