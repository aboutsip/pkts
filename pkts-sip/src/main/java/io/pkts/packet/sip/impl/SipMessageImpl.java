/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.header.impl.CSeqHeaderImpl;
import io.pkts.packet.sip.header.impl.CallIdHeaderImpl;
import io.pkts.packet.sip.header.impl.ContactHeaderImpl;
import io.pkts.packet.sip.header.impl.ContentTypeHeaderImpl;
import io.pkts.packet.sip.header.impl.FromHeaderImpl;
import io.pkts.packet.sip.header.impl.MaxForwardsHeaderImpl;
import io.pkts.packet.sip.header.impl.RecordRouteHeaderImpl;
import io.pkts.packet.sip.header.impl.RouteHeaderImpl;
import io.pkts.packet.sip.header.impl.ToHeaderImpl;
import io.pkts.packet.sip.header.impl.ViaHeaderImpl;
import io.pkts.sdp.SDPFactory;
import io.pkts.sdp.SdpException;
import io.pkts.sdp.SdpParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    /**
     * The initial line of the sip message, which is either a request or a
     * response line
     */
    private SipInitialLine initialLine;

    /**
     * The unparsed initial line.
     */
    private final Buffer rawInitialLine;

    /**
     * All the headers of the sip message
     */
    private final Buffer headers;

    /**
     * Stupid, just to fix it quickly and since a sliced buffer is kind of cheap
     * perhaps it is ok for now
     */
    private Buffer headersCopy;

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
    private final Map<Buffer, List<SipHeader>> parsedHeaders = new LinkedHashMap<Buffer, List<SipHeader>>(16, 0.75f);

    /**
     * 
     * @param rawInitialBuffer
     *            the raw initial line, which is either a request or a response
     *            line (hopefully anyway, we won't know until we try!_
     * @param headers
     *            all the headers (un-parsed) of the SIP message
     * @param payload
     *            the payload or null if there is none
     */
    public SipMessageImpl(final Buffer rawInitialBuffer, final Buffer headers, final Buffer payload) {
        this.rawInitialLine = rawInitialBuffer;
        this.headers = headers;
        if (headers != null) {
            this.headersCopy = headers.slice();
        }
        this.payload = payload;
    }

    /**
     * 
     * @param initialLine
     *            the initial line, which is either a request or a response line
     * @param headers
     *            all the headers (un-parsed) of the SIP message
     * @param payload
     *            the payload or null if there is none
     */
    public SipMessageImpl(final SipInitialLine initialLine, final Buffer headers, final Buffer payload) {
        this.initialLine = initialLine;
        this.rawInitialLine = null;
        this.headers = headers;
        if (headers != null) {
            this.headersCopy = headers.slice();
        }
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getInitialLine() {
        if (this.initialLine == null) {
            return this.rawInitialLine;
        }

        return this.initialLine.getBuffer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isResponse() {
        return getInitialLineInternal().isResponseLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isRequest() {
        return getInitialLineInternal().isRequestLine();
    }

    private SipInitialLine getInitialLineInternal() {
        if (this.initialLine == null) {
            this.initialLine = SipInitialLine.parse(this.rawInitialLine);
        }
        return this.initialLine;
    }

    /**
     * Since everything is done lazily, this method will parse and return the
     * initial line as a {@link SipRequestLine}. Only meant to be used by the
     * sub-classes.
     * 
     * Of course, this method could throw both {@link ClassCastException} in
     * case this is actually a response and the parsing of the request line
     * could also fail.
     * 
     * @return
     */
    protected SipRequestLine getRequestLine() throws SipParseException, ClassCastException {
        return (SipRequestLine) getInitialLineInternal();
    }

    /**
     * Same as {@link #getRequestLine()} but for {@link SipResponseLine}
     * instead.
     * 
     * @return
     * @throws SipParseException
     * @throws ClassCastException
     */
    protected SipResponseLine getResponseLine() throws SipParseException, ClassCastException {
        return (SipResponseLine) getInitialLineInternal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SipHeader getHeader(final Buffer headerName) throws SipParseException {
        List<SipHeader> headers = this.parsedHeaders.get(headerName);
        final SipHeader h = headers == null || headers.isEmpty() ? null : headers.get(0);
        if (h != null) {
            return h;
        }

        while (this.headers != null && this.headers.hasReadableBytes()) {
            final List<SipHeader> nextHeaders = SipParser.nextHeaders(this.headers);
            if (nextHeaders == null || nextHeaders.isEmpty()) {
                return null;
            }

            final Buffer currentHeaderName = nextHeaders.get(0).getName();
            headers = this.parsedHeaders.get(currentHeaderName);
            if (headers == null) {
                this.parsedHeaders.put(currentHeaderName, nextHeaders);
            } else {
                headers.addAll(nextHeaders);
            }

            if (currentHeaderName.equals(headerName)) {
                return nextHeaders.get(0);
            }
        }

        // didn't find the header that was requested
        return null;
    }

    @Override
    public void addHeader(final SipHeader header) {
        internalAddHeader(header, false);
    }

    /**
     * There are situations, such as when user does a
     * {@link #setHeader(SipHeader)}, where we must frame all headers, this
     * method does that.
     * 
     * @throws SipParseException
     */
    private void frameAllHeaders() throws SipParseException {
        while (this.headers != null && this.headers.hasReadableBytes()) {
            final List<SipHeader> nextHeaders = SipParser.nextHeaders(this.headers);
            if (nextHeaders == null || nextHeaders.isEmpty()) {
                return;
            }

            final Buffer currentHeaderName = nextHeaders.get(0).getName();
            final List<SipHeader> headers = this.parsedHeaders.get(currentHeaderName);

            if (headers == null) {
                this.parsedHeaders.put(currentHeaderName, nextHeaders);
            } else {
                headers.addAll(nextHeaders);
            }
        }
    }

    private void internalAddHeader(final SipHeader header, final boolean addFirst) {
        List<SipHeader> headers = this.parsedHeaders.get(header.getName());
        if (headers == null) {
            headers = new ArrayList<SipHeader>();
            this.parsedHeaders.put(header.getName(), headers);
        }

        if (addFirst) {
            headers.add(0, header);
        } else {
            headers.add(header);
        }
    }

    @Override
    public void addHeaderFirst(final SipHeader header) throws SipParseException {
        internalAddHeader(header, true);
    }

    @Override
    public void setHeader(final SipHeader header) throws SipParseException {
        frameAllHeaders();
        final List<SipHeader> headers = new ArrayList<SipHeader>();
        headers.add(header);
        this.parsedHeaders.put(header.getName(), headers);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public SipHeader getHeader(final String headerName) throws SipParseException {
        return getHeader(Buffers.wrap(headerName));
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
        final List<SipHeader> headers = this.parsedHeaders.get(from.getName());
        headers.set(0, from);
        return from;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViaHeader getViaHeader() throws SipParseException {
        final SipHeader header = getHeader(ViaHeader.NAME);
        if (header instanceof ViaHeader) {
            return (ViaHeader) header;
        }

        if (header == null) {
            return null;
        }

        final Buffer buffer = header.getValue();
        final ViaHeader via = ViaHeaderImpl.frame(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(via.getName());
        headers.set(0, via);
        return via;
    }

    @Override
    public List<ViaHeader> getViaHeaders() throws SipParseException {
        frameAllHeaders();
        final List<SipHeader> headers = this.parsedHeaders.get(ViaHeader.NAME);
        final List<ViaHeader> vias = new ArrayList<ViaHeader>(headers.size());
        for (int i = 0; i < headers.size(); ++i) {
            final SipHeader header = headers.get(i);
            if (header instanceof ViaHeader) {
                vias.add((ViaHeader) header);
            } else {
                final Buffer buffer = header.getValue();
                final ViaHeader via = ViaHeaderImpl.frame(buffer);
                headers.set(i, via);
                vias.add(via);
            }
        }
        return vias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordRouteHeader getRecordRouteHeader() throws SipParseException {
        final SipHeader header = getHeader(RecordRouteHeader.NAME);
        if (header instanceof RecordRouteHeader) {
            return (RecordRouteHeader) header;
        }

        if (header == null) {
            return null;
        }

        final Buffer buffer = header.getValue();
        final RecordRouteHeader rr = RecordRouteHeaderImpl.frame(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(rr.getName());
        headers.set(0, rr);
        return rr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouteHeader getRouteHeader() throws SipParseException {
        final SipHeader header = getHeader(RouteHeader.NAME);
        if (header instanceof RouteHeader) {
            return (RouteHeader) header;
        }

        if (header == null) {
            return null;
        }

        final Buffer buffer = header.getValue();
        final RouteHeader route = RouteHeaderImpl.frame(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(route.getName());
        headers.set(0, route);
        return route;
    }

    @Override
    public MaxForwardsHeader getMaxForwards() throws SipParseException {
        final SipHeader header = getHeader(MaxForwardsHeader.NAME);
        if (header instanceof MaxForwardsHeader) {
            return (MaxForwardsHeader) header;
        }

        if (header == null) {
            return null;
        }

        final Buffer buffer = header.getValue();
        final MaxForwardsHeader max = MaxForwardsHeaderImpl.frame(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(max.getName());
        headers.set(0, max);
        return max;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContactHeader getContactHeader() throws SipParseException {
        final SipHeader header = getHeader(ContactHeader.NAME);
        if (header instanceof ContactHeader) {
            return (ContactHeader) header;
        }

        if (header == null) {
            return null;
        }

        final Buffer buffer = header.getValue();
        final ContactHeader contact = ContactHeaderImpl.frame(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(contact.getName());
        headers.set(0, contact);
        return contact;
    }

    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipParseException {
        final SipHeader header = getHeader(ContentTypeHeader.NAME);
        if (header instanceof ContentTypeHeader) {
            return (ContentTypeHeader) header;
        }

        final Buffer buffer = header.getValue();
        final ContentTypeHeader ct = ContentTypeHeaderImpl.frame(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(ct.getName());
        headers.set(0, ct);
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
        final List<SipHeader> headers = this.parsedHeaders.get(to.getName());
        headers.set(0, to);
        return to;
    }

    @Override
    public CSeqHeader getCSeqHeader() throws SipParseException {
        final SipHeader header = getHeader(CSeqHeader.NAME);
        if (header instanceof CSeqHeader) {
            return (CSeqHeader) header;
        }

        final Buffer buffer = header.getValue();
        final CSeqHeader cseq = CSeqHeaderImpl.parseValue(buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(cseq.getName());
        headers.set(0, cseq);
        return cseq;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallIdHeader getCallIDHeader() throws SipParseException {
        SipHeader header = getHeader(CallIdHeader.NAME);
        if (header instanceof CallIdHeader) {
            return (CallIdHeader) header;
        }
        boolean compactForm = false;

        if (header == null) {
            header = getHeader(CallIdHeader.COMPACT_NAME);
            if (header != null) {
                compactForm = true;
            }
        }

        if (header == null) {
            throw new SipParseException(0, "The Call-ID header is missing. Bad SIP message!");
        }

        final Buffer buffer = header.getValue();
        final CallIdHeader callId = CallIdHeaderImpl.frame(compactForm, buffer);
        final List<SipHeader> headers = this.parsedHeaders.get(callId.getName());
        headers.set(0, callId);
        return callId;
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
        final ToHeader to = getToHeader();
        return to.getTag() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInvite() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'I' && m.getByte(1) == 'N' && m.getByte(2) == 'V' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'T' && m.getByte(5) == 'E';
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
            return m.getByte(0) == 'B' && m.getByte(1) == 'Y' && m.getByte(2) == 'E';
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
            return m.getByte(0) == 'A' && m.getByte(1) == 'C' && m.getByte(2) == 'K';
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
            return m.getByte(0) == 'C' && m.getByte(1) == 'A' && m.getByte(2) == 'N' && m.getByte(3) == 'C'
                    && m.getByte(4) == 'E' && m.getByte(5) == 'L';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public boolean isOptions() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'O' && m.getByte(1) == 'P' && m.getByte(2) == 'T' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'O' && m.getByte(5) == 'N' && m.getByte(6) == 'S';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public boolean isMessage() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'M' && m.getByte(1) == 'E' && m.getByte(2) == 'S' && m.getByte(3) == 'S'
                    && m.getByte(4) == 'A' && m.getByte(5) == 'G' && m.getByte(6) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public boolean isInfo() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'I' && m.getByte(1) == 'N' && m.getByte(2) == 'F' && m.getByte(3) == 'O';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public String toString() {
        return toBuffer().toString();
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
            throw new SipParseException(e.getErrorOffset(), "Unable to process the Content-Type header", e);
        }
    }

    @Override
    public final Buffer getRawContent() {
        if (!hasContent()) {
            return null;
        }

        return this.payload.slice();
    }

    @Override
    public final boolean hasContent() {
        return this.payload != null && this.payload.hasReadableBytes();
    }

    @Override
    public SipRequest toRequest() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    @Override
    public SipResponse toResponse() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipResponse");
    }

    @Override
    public SipResponse createResponse(final int responseCode) throws SipParseException, ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    @Override
    public Buffer toBuffer() {
        final Buffer buffer = Buffers.createBuffer(2048);
        getInitialLine().getBytes(buffer);
        buffer.write(SipParser.CR);
        buffer.write(SipParser.LF);
        transferHeaders(buffer);
        if (this.payload != null) {
            this.payload.getBytes(0, buffer);
        }
        return buffer;
    }

    /**
     * Helper method to clone all the headers into one continuous buffer.
     * 
     * @return
     */
    protected Buffer cloneHeaders() {
        if (this.headers.getReaderIndex() == 0) {
            return this.headers.clone();
        }

        final Buffer headerClone = Buffers.createBuffer(this.headers.capacity() + 200);
        transferHeaders(headerClone);
        return headerClone;
    }

    /**
     * Helper method to clone the payload.
     * 
     * @return
     */
    protected Buffer clonePayload() {
        if (this.payload != null) {
            return this.payload.clone();
        }

        return null;
    }

    /**
     * Transfer the data of all headers into the supplied buffer.
     * 
     * @param dst
     */
    protected void transferHeaders(final Buffer dst) {
        for (final Entry<Buffer, List<SipHeader>> headers : this.parsedHeaders.entrySet()) {
            for (final SipHeader header : headers.getValue()) {
                header.getBytes(dst);
                dst.write(SipParser.CR);
                dst.write(SipParser.LF);
            }
        }

        if (this.headers != null) {
            this.headers.getBytes(dst);
        }
    }

    @Override
    public abstract SipMessage clone();

}
