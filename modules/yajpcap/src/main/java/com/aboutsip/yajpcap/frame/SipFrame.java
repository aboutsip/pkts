/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.impl.SipInitialLine;
import com.aboutsip.yajpcap.packet.sip.impl.SipRequestImpl;
import com.aboutsip.yajpcap.packet.sip.impl.SipRequestLine;
import com.aboutsip.yajpcap.packet.sip.impl.SipResponseImpl;
import com.aboutsip.yajpcap.packet.sip.impl.SipResponseLine;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * A frame knowledgeable of the SIP protocol.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipFrame extends AbstractFrame implements Layer7Frame {

    private final Layer4Frame parentFrame;

    private final Buffer initialLine;

    private final Buffer headers;

    /**
     * @param framerManager
     * @param p
     * @param payload
     */
    public SipFrame(final FramerManager framerManager, final Layer4Frame parentFrame, final Buffer initalLine,
            final Buffer headers,
            final Buffer payload) {
        super(framerManager, Protocol.SIP, payload);
        assert parentFrame != null;
        this.parentFrame = parentFrame;
        this.initialLine = initalLine;
        this.headers = headers;
    }

    /**
     * Get the raw headers in this sip frame
     * 
     * @return
     */
    public Buffer getHeaders() {
        return this.headers;
    }

    /**
     * Get the initial line of this sip frame. It should either be a request
     * line or a response line but we are actually not sure until we have parsed
     * the content of what has been framed
     * 
     * @return
     */
    public Buffer getInitialLine() {
        return this.initialLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        final Protocol p = getPayloadProtocol(payload);
        if (p == null) {
            // no payload so nothing to frame
            return null;
        }

        // TODO: should probably throw an exception instead to let the user
        // know that there is a payload present but we don't know what to do
        // with it
        if (p == Protocol.UNKNOWN) {
            return null;
        }

        final Framer framer = framerManager.getFramer(p);
        if (framer == null) {
            // throw exception, don't know how to frame this protocol
        }

        return framer.frame(this, payload);
    }

    /**
     * Parse the content into a "real" SIP message. The parser method will just
     * make sure that we have the basic information available before
     * constructing a packet. Remember, everything is done lazily so if the user
     * really needs to know if the packet conforms to SIP rules, then he/she
     * should call {@inheritDoc}
     */
    @Override
    public SipMessage parse() throws PacketParseException {
        final TransportPacket pkt = this.parentFrame.parse();
        final SipInitialLine initialLine = SipInitialLine.parse(this.initialLine);
        if (initialLine.isRequestLine()) {
            return new SipRequestImpl(pkt, (SipRequestLine) initialLine, this.headers, getPayload(), this);
        }
        return new SipResponseImpl(pkt, (SipResponseLine) initialLine, this.headers, getPayload(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        this.parentFrame.writeExternal(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.parentFrame.write(out);
    }

    /**
     * Check what type of protocol, if any, is in the body of the sip message
     * 
     * @return
     */
    private Protocol getPayloadProtocol(final Buffer payload) throws IOException {
        if ((payload == null) || !payload.hasReadableBytes()) {
            return null;
        }

        final Buffer contentTypeHeader = getContentTypeHeader(this.headers);
        if (contentTypeHeader == null) {
            // this is an error. We apparently have a payload but
            // there is no content-type header. Throw some kind
            // of parse exception? Or leave it up to the parsing step?
            return null;
        }

        final Buffer contentType = getContentType(contentTypeHeader);
        if (contentType.isEmpty()) {
            // yet another error in the sip message. Same question as above...
            return null;
        }

        // TODO: add a trim function to Buffer
        // Also, probably should move this mapping somewhere else, perhaps into
        // the Protocol enum?
        final String t = contentType.toString().trim();
        if ("application/sdp".equals(t)) {
            return Protocol.SDP;
        }

        // uknown for now
        return Protocol.UNKNOWN;
    }

    private Buffer getContentType(final Buffer contentTypeHeader) throws IOException {
        contentTypeHeader.markReaderIndex();
        while (contentTypeHeader.hasReadableBytes() && (contentTypeHeader.readByte() != ':')) {
            // do nothing
        }
        final Buffer contentType = contentTypeHeader.slice();
        contentTypeHeader.resetReaderIndex();
        return contentType;
    }

    private Buffer getContentTypeHeader(final Buffer headers) throws IOException {
        headers.markReaderIndex();

        Buffer contentType = null;
        Buffer line = null;
        while ((line = headers.readLine()) != null) {
            // quick first test
            final byte a = line.getByte(0);
            final byte b = line.getByte(1);
            final byte c = line.getByte(2);

            // could be Content-Type, but also other things so check
            if ((a == 'C') && (b == 'o') && (c == 'n')) {
                final String tmp = line.toString();
                if (tmp.startsWith("Content-Type")) {
                    contentType = line;
                    break;
                }
            } else if ((a == 'c') && (b == ':')) {
                // compact form of content type. Note though, there could
                // actually be spaces before the ':' but we'll ignore that case
                // for now
                contentType = line;
            }
        }
        headers.resetReaderIndex();
        return contentType;
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
