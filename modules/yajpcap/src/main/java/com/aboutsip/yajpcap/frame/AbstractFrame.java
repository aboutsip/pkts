/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class AbstractFrame implements Frame {

    /**
     * The framer manager we use to lookup framers for protocols
     */
    private final FramerManager framerManager;

    /**
     * The protocol of this frame
     */
    private final Protocol protocol;

    /**
     * A frame may contain additional frames, which are carried within the
     * payload. Note, not all frames have payloads.
     */
    private final Buffer payload;

    /**
     * The next frame.
     */
    private Frame nextFrame;

    /**
     * 
     */
    public AbstractFrame(final FramerManager framerManager, final Protocol p, final Buffer payload) {
        assert framerManager != null;
        assert p != null;
        this.framerManager = framerManager;
        this.protocol = p;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasProtocol(final Protocol p) throws IOException {
        return getFrame(p) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Frame getFrame(final Protocol p) throws IOException {
        if (this.protocol == p) {
            return this;
        }

        final Frame next = getNextFrame();
        if (next != null) {
            return next.getFrame(p);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void frameAll() throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName() {
        return this.protocol.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Frame getNextFrame() throws IOException {
        if (this.nextFrame == null) {
            this.nextFrame = framePayload(this.framerManager, this.payload);
        }

        return this.nextFrame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Buffer getData() {
        return this.payload;
    }

    /**
     * Each implementing frame needs to figure out how to frame its payload.
     * 
     * @param payload
     * @return
     */
    protected abstract Frame framePayload(FramerManager framerManager, Buffer payload) throws IOException;


}
