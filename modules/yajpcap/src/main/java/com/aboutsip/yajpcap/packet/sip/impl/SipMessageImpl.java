/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.sdp.SDPFactory;
import com.aboutsip.sdp.SdpException;
import com.aboutsip.sdp.SdpParseException;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.header.ContentTypeHeader;
import com.aboutsip.yajpcap.packet.sip.header.FromHeader;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;
import com.aboutsip.yajpcap.packet.sip.header.impl.ContentTypeHeaderImpl;
import com.aboutsip.yajpcap.packet.sip.header.impl.FromHeaderImpl;
import com.aboutsip.yajpcap.packet.sip.header.impl.ToHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class SipMessageImpl implements SipMessage {


    public static final Buffer FROM_HEADER = Buffers.wrap("From".getBytes());

    public static final Buffer TO_HEADER = Buffers.wrap("To".getBytes());

    public static final Buffer Call_ID_HEADER = Buffers.wrap("Call-ID".getBytes());

    public static final Buffer CSEQ_HEADER = Buffers.wrap("CSeq".getBytes());

    private final SDPFactory sdpFactory = SDPFactory.getInstance();

    private final TransportPacket parent;

    /**
     * The initial line of the sip message, which is either a request or a
     * response line
     */
    private final SipInitialLine initialLine;

    /**
     * All the headers of the sip message
     */
    private final Buffer headers;

    /**
     * Stupid, just to fix it quickly and since a sliced buffer
     * is kind of cheap perhaps it is ok for now
     */
    private final Buffer headersCopy;

    /**
     * The payload, which may be null
     */
    private final Buffer payload;

    /**
     * Map with parsed headers. Need to change since there are many headers that
     * can appear multiple times. We'll get to that...
     * 
     * We'll keep the default size of 16 and load factory of 0.75, which means
     * that we won't do a re-hash until we hit 12 headers. A basic request has
     * around 10ish headers but in real life there will be much more so get some
     * real world examples and set an appropriate size based on that.
     */
    private final Map<Buffer, SipHeader> parsedHeaders = new HashMap<Buffer, SipHeader>(16, 0.75f);

    /**
     * Our raw frame and if set and the message isn't marked as dirty we will
     * actually use the data in the frame when we are asked to write ourselves
     * to an output stream.
     */
    private final SipFrame sipFrame;

    /**
     * 
     * @param initialLine the initial line, which is either a request or a
     *            response line
     * @param headers all the headers (un-parsed) of the SIP message
     * @param payload the payload or null if there is none
     */
    public SipMessageImpl(final TransportPacket parent, final SipInitialLine initialLine, final Buffer headers,
            final Buffer payload, final SipFrame sipFrame) {
        assert initialLine != null;
        assert headers != null;
        assert parent != null;

        this.parent = parent;
        this.initialLine = initialLine;
        this.headers = headers;
        this.headersCopy = headers.slice();
        this.payload = payload;
        this.sipFrame = sipFrame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getInitialLine() {
        return this.initialLine.getBuffer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isResponse() {
        return this.initialLine.isResponseLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isRequest() {
        return this.initialLine.isRequestLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SipHeader getHeader(final Buffer headerName) throws SipParseException {
        final SipHeader h = this.parsedHeaders.get(headerName);
        if (h != null) {
            return h;
        }

        while (this.headers.hasReadableBytes()) {
            final SipHeader header = SipParser.nextHeader(this.headers);
            if (header == null) {
                return null;
            }
            this.parsedHeaders.put(header.getName(), header);
            if (header.getName().equals(headerName)) {
                return header;
            }
        }

        // didn't find the header that was requested
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FromHeader getFromHeader() throws SipParseException {
        final SipHeader header = getHeader(FromHeader.NAME);
        if (header instanceof FromHeader) {
            return (FromHeader) header;
        }

        final Buffer buffer = header.getValue();
        final FromHeader from = FromHeaderImpl.frame(buffer);
        this.parsedHeaders.put(from.getName(), from);
        return from;
    }

    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipParseException {
        final SipHeader header = getHeader(ContentTypeHeader.NAME);
        if (header instanceof ContentTypeHeader) {
            return (ContentTypeHeader) header;
        }

        final Buffer buffer = header.getValue();
        final ContentTypeHeader ct = ContentTypeHeaderImpl.frame(buffer);
        this.parsedHeaders.put(ct.getName(), ct);
        return ct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToHeader getToHeader() throws SipParseException {
        final SipHeader header = getHeader(ToHeader.NAME);
        if (header instanceof ToHeader) {
            return (ToHeader) header;
        }

        final Buffer buffer = header.getValue();
        final ToHeader to = ToHeaderImpl.frame(buffer);
        this.parsedHeaders.put(to.getName(), to);
        return to;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SipHeader getCallIDHeader() throws SipParseException {
        return getHeader(Call_ID_HEADER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Buffer getMethod() throws SipParseException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitial() throws SipParseException {

        // over simplified check
        final SipHeader to = getToHeader();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInvite() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return (m.getByte(0) == 'I') && (m.getByte(1) == 'N') && (m.getByte(2) == 'V') && (m.getByte(3) == 'I')
                    && (m.getByte(4) == 'T') && (m.getByte(5) == 'E');
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBye() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return (m.getByte(0) == 'B') && (m.getByte(1) == 'Y') && (m.getByte(2) == 'E');
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAck() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return (m.getByte(0) == 'A') && (m.getByte(1) == 'C') && (m.getByte(2) == 'K');
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancel() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return (m.getByte(0) == 'C') && (m.getByte(1) == 'A') && (m.getByte(2) == 'N') && (m.getByte(3) == 'C')
                    && (m.getByte(4) == 'E') && (m.getByte(5) == 'L');
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.initialLine.toString()).append("\n");
        sb.append(this.headersCopy.toString()).append("\n");
        if (this.payload != null) {
            sb.append(this.payload.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public final Object getContent() throws SipParseException {
        if (!hasContent()) {
            return null;
        }

        try {
            final ContentTypeHeader contentType = getContentTypeHeader();
            if (contentType == null) {
                return null;
            }

            if (contentType.isSDP()) {
                try {
                    return this.sdpFactory.parse(this.payload);
                } catch (final SdpParseException e) {
                    throw new SipParseException(e.getCharOffset(), e.getMessage(), e);
                } catch (final SdpException e) {
                    throw new SipParseException(0, "Unable to parse the content as an SDP", e);
                }
            }
            return this.payload;
        } catch (final SipParseException e) {
            throw new SipParseException(e.getErroOffset(), "Unable to process the Content-Type header", e);
        }
    }

    @Override
    public final boolean hasContent() {
        return (this.payload != null) && this.payload.hasReadableBytes();
    }

    @Override
    public final long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public final int getSourcePort() {
        return this.parent.getSourcePort();
    }

    @Override
    public final int getDestinationPort() {
        return this.parent.getDestinationPort();
    }

    @Override
    public final String getSourceIP() {
        return this.parent.getSourceIP();
    }

    @Override
    public final String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    @Override
    public final String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    @Override
    public final String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    @Override
    public int getTotalLength() {
        return this.parent.getTotalLength();
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        // TODO: this only works when the message has not been modified.
        // since you cannot modify a SipMessage right now anyway (well, not
        // entirely true) we'll stick with this for now.
        this.sipFrame.write(out);
    }
}
