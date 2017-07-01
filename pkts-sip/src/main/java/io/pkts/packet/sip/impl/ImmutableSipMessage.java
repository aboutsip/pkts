package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentLengthHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.ExpiresHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ImmutableSipMessage implements SipMessage {

    private static final String I_AM_IMMUTABLE_NO_CAN_DO = "I am immutable, no can do";
    private final Buffer message;
    private final SipInitialLine initialLine;
    private final List<SipHeader> headers;
    private final Buffer body;

    private final short indexOfTo;
    private final short indexOfFrom;
    private final short indexOfCSeq;
    private final short indexOfCallId;
    private final short indexOfMaxForwards;
    private final short indexOfVia;
    private final short indexOfRoute;
    private final short indexOfRecordRoute;
    private final short indexOfContact;

    /**
     *
     * @param message the full immutable buffer which has the entire SIP message in it, including all headers, body
     *                initial line etc.
     * @param initialLine the parsed initial line (which is just a reference into the message buffer)
     * @param headers
     * @param body
     */
    protected ImmutableSipMessage(final Buffer message,
                                  final SipInitialLine initialLine,
                                  final List<SipHeader> headers,
                                  final short indexOfTo,
                                  final short indexOfFrom,
                                  final short indexOfCSeq,
                                  final short indexOfCallId,
                                  final short indexOfMaxForwards,
                                  final short indexOfVia,
                                  final short indexOfRoute,
                                  final short indexOfRecordRoute,
                                  final short indexOfContact,
                                  final Buffer body) {
        this.message = message;
        this.initialLine = initialLine;
        this.headers = headers;
        this.body = body;

        this.indexOfTo = indexOfTo;
        this.indexOfFrom = indexOfFrom;
        this.indexOfCSeq = indexOfCSeq;
        this.indexOfCallId = indexOfCallId;
        this.indexOfMaxForwards = indexOfMaxForwards;
        this.indexOfVia = indexOfVia;
        this.indexOfRoute = indexOfRoute;
        this.indexOfRecordRoute = indexOfRecordRoute;
        this.indexOfContact = indexOfContact;
    }

    @Override
    public SipInitialLine initialLine() {
        return this.initialLine;
    }

    @Override
    public String toString() {
        return this.message.toString();
    }

    @Override
    public List<SipHeader> getAllHeaders() {
        return new ArrayList<>(headers);
    }

    @Override
    public int countNoOfHeaders() {
        return headers.size();
    }

    @Override
    public Buffer getInitialLine() {
        return initialLine.getBuffer();
    }

    protected SipInitialLine getInitialLineAsObject() {
        return initialLine;
    }

    @Override
    public Buffer getContent() {
        return body;
    }

    @Override
    public boolean hasContent() {
        return body != null;
    }

    @Override
    public Optional<SipHeader> getHeader(final Buffer headerName) throws SipParseException {
        return Optional.ofNullable(findHeader(headerName));
    }

    @Override
    public List<SipHeader> getHeaders(final Buffer headerName) throws SipParseException {
        PreConditions.assertNotEmpty(headerName, "The name of the header cannot be null or the empty buffer");
        return getHeadersInternal(headerName);
    }

    @Override
    public List<SipHeader> getHeaders(final String headerName) throws SipParseException {
        PreConditions.assertNotEmpty(headerName, "The name of the header cannot be null or the empty string");
        return getHeadersInternal(Buffers.wrap(headerName));
    }

    private List<SipHeader> getHeadersInternal(final Buffer headerName) {
        final List<SipHeader> headers = new ArrayList<>(3);
        for (final SipHeader header : this.headers) {
            if (headerName.equals(header.getName())) {
                headers.add(header);
            }
        }
        return headers;
    }

    @Override
    public Optional<SipHeader> getHeader(final String headerName) throws SipParseException {
        return Optional.ofNullable(findHeader(Buffers.wrap(headerName)));
    }

    @Override
    public void addHeader(final SipHeader header) throws SipParseException {
        throw new RuntimeException(I_AM_IMMUTABLE_NO_CAN_DO);
    }

    @Override
    public void addHeaderFirst(SipHeader header) throws SipParseException {
        throw new RuntimeException(I_AM_IMMUTABLE_NO_CAN_DO);

    }

    @Override
    public SipHeader popHeader(Buffer headerNme) throws SipParseException {
        throw new RuntimeException(I_AM_IMMUTABLE_NO_CAN_DO);
    }

    @Override
    public void setHeader(SipHeader header) throws SipParseException {
        throw new RuntimeException(I_AM_IMMUTABLE_NO_CAN_DO);

    }

    @Override
    public FromHeader getFromHeader() throws SipParseException {
        if (indexOfFrom != -1) {
            return headers.get(indexOfFrom).ensure().toFromHeader();
        }
        return null;
    }

    @Override
    public ToHeader getToHeader() throws SipParseException {
        if (indexOfTo != -1) {
            return headers.get(indexOfTo).ensure().toToHeader();
        }
        return null;
    }

    @Override
    public ViaHeader getViaHeader() throws SipParseException {
        if (indexOfVia != -1) {
            return headers.get(indexOfVia).ensure().toViaHeader();
        }
        return null;
    }

    @Override
    public List<ViaHeader> getViaHeaders() throws SipParseException {
        if (indexOfVia == -1) {
            return Collections.emptyList();
        }

        final List<ViaHeader> vias = new ArrayList<>(5);
        vias.add(headers.get(indexOfVia).ensure().toViaHeader());
        for (int i = indexOfVia + 1; i < headers.size(); ++i) {
            final SipHeader h = headers.get(i);
            if (h.isViaHeader()) {
                vias.add(h.ensure().toViaHeader());
            }
        }

        return vias;
    }

    @Override
    public MaxForwardsHeader getMaxForwards() throws SipParseException {
        if (indexOfMaxForwards != -1) {
            return headers.get(indexOfMaxForwards).ensure().toMaxForwardsHeader();
        }
        return null;
    }

    @Override
    public RecordRouteHeader getRecordRouteHeader() throws SipParseException {
        if (indexOfRecordRoute != -1) {
            return headers.get(indexOfRecordRoute).ensure().toRecordRouteHeader();
        }
        return null;
    }

    @Override
    public List<RecordRouteHeader> getRecordRouteHeaders() throws SipParseException {
        if (indexOfRecordRoute == -1) {
            return Collections.emptyList();
        }

        final List<RecordRouteHeader> routes = new ArrayList<>(5);
        routes.add(headers.get(indexOfRecordRoute).ensure().toRecordRouteHeader());
        for (int i = indexOfRecordRoute + 1; i < headers.size(); ++i) {
            final SipHeader h = headers.get(i);
            if (h.isRecordRouteHeader()) {
                routes.add(h.ensure().toRecordRouteHeader());
            }
        }

        return routes;
    }

    @Override
    public RouteHeader getRouteHeader() throws SipParseException {
        if (indexOfRoute != -1) {
            return headers.get(indexOfRoute).ensure().toRouteHeader();
        }
        return null;
    }

    @Override
    public List<RouteHeader> getRouteHeaders() throws SipParseException {
        if (indexOfRoute == -1) {
            return Collections.emptyList();
        }

        final List<RouteHeader> routes = new ArrayList<>(5);
        routes.add(headers.get(indexOfRoute).ensure().toRouteHeader());
        for (int i = indexOfRoute + 1; i < headers.size(); ++i) {
            final SipHeader h = headers.get(i);
            if (h.isRouteHeader()) {
                routes.add(h.ensure().toRouteHeader());
            }
        }

        return routes;
    }

    @Override
    public ExpiresHeader getExpiresHeader() throws SipParseException {
        final SipHeader header = findHeader(ExpiresHeader.NAME);
        if (header == null) {
            return null;
        }

        return header.ensure().toExpiresHeader();
    }

    @Override
    public ContactHeader getContactHeader() throws SipParseException {
        if (indexOfContact != -1) {
            return headers.get(indexOfContact).ensure().toContactHeader();
        }
        return null;
    }

    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipParseException {
        final SipHeader header = findHeader(ContentTypeHeader.NAME);
        if (header == null) {
            return null;
        }

        return header.ensure().toContentTypeHeader();
    }

    @Override
    public int getContentLength() throws SipParseException {
        final SipHeader header = findHeader(ContentLengthHeader.NAME);
        if (header == null) {
            return 0;
        }

        return header.ensure().toContentLengthHeader().getContentLength();
    }

    @Override
    public CallIdHeader getCallIDHeader() throws SipParseException {
        if (indexOfCallId != -1) {
            return headers.get(indexOfCallId).ensure().toCallIdHeader();
        }
        return null;
    }

    @Override
    public CSeqHeader getCSeqHeader() throws SipParseException {
        if (indexOfCSeq != -1) {
            return headers.get(indexOfCSeq).ensure().toCSeqHeader();
        }
        return null;
    }

    @Override
    public boolean isInitial() throws SipParseException {
        final ToHeader to = getToHeader();
        return to.getTag() == null;
    }

    @Override
    public void verify() {

    }

    private SipHeader findHeader(final Buffer name) {
        for (final SipHeader header : headers) {
            if (name.equals(header.getName())) {
                return header;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        try {
            final SipMessage other = (SipMessage)o;
            if (!initialLine.equals(other.initialLine())) {
                return false;
            }

            // TODO: lots more

            return true;
        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public Buffer toBuffer() {
        return message;
    }

    @Override
    public SipMessage clone() {
        return this;
    }
}
