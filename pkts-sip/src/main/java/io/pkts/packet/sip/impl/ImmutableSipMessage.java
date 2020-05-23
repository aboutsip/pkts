package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
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
import java.util.Map;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ImmutableSipMessage implements SipMessage {

    private static final String I_AM_IMMUTABLE_NO_CAN_DO = "I am immutable, no can do";
    private final Buffer message;
    private final SipInitialLine initialLine;
    private final Map<String, List<SipHeader>> headers;
    private final Buffer body;
    private final SipHeader toHeader;
    private final SipHeader fromHeader;
    private final SipHeader cSeqHeader;
    private final SipHeader callIdHeader;
    private final SipHeader maxForwardsHeader;
    private final SipHeader viaHeader;
    private final SipHeader routeHeader;
    private final SipHeader recordRouteHeader;
    private final SipHeader contactHeader;

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
                                  final Map<String, List<SipHeader>> headers,
                                  final SipHeader toHeader,
                                  final SipHeader fromHeader,
                                  final SipHeader cSeqHeader,
                                  final SipHeader callIdHeader,
                                  final SipHeader maxForwardsHeader,
                                  final SipHeader viaHeader,
                                  final SipHeader routeHeader,
                                  final SipHeader recordRouteHeader,
                                  final SipHeader contactHeader,
                                  final Buffer body) {
        this.message = message;
        this.initialLine = initialLine;
        this.headers = headers;
        this.body = body;
        this.toHeader = toHeader;
        this.fromHeader = fromHeader;
        this.cSeqHeader = cSeqHeader;
        this.callIdHeader = callIdHeader;
        this.maxForwardsHeader = maxForwardsHeader;
        this.viaHeader = viaHeader;
        this.routeHeader = routeHeader;
        this.recordRouteHeader = recordRouteHeader;
        this.contactHeader = contactHeader;
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
        final List<SipHeader> allHeaders = new ArrayList<>();
        for(final List<SipHeader> headerValues : headers.values()) {
            allHeaders.addAll(headerValues);
        }

        return allHeaders;
    }

    @Override
    public Map<String, List<SipHeader>> getHeaderValues() {

        return headers;
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
        return getHeader(headerName.toString());
    }

    @Override
    public List<SipHeader> getHeaders(final Buffer headerName) throws SipParseException {
        PreConditions.assertNotEmpty(headerName, "The name of the header cannot be null or the empty buffer");
        return getHeaders(headerName.toString());
    }

    @Override
    public List<SipHeader> getHeaders(final String headerName) throws SipParseException {
        PreConditions.assertNotEmpty(headerName, "The name of the header cannot be null or the empty string");

        final List<SipHeader> headerValues = headers.get(headerName);

        return headerValues == null || headerValues.isEmpty() ? Collections.emptyList() : new ArrayList<>(headerValues);
    }


    @Override
    public Optional<SipHeader> getHeader(final String headerName) throws SipParseException {
        return Optional.ofNullable(findHeader(headerName));
    }

    @Override
    public FromHeader getFromHeader() throws SipParseException {
        return fromHeader != null ? fromHeader.ensure().toFromHeader() : null;
    }

    @Override
    public ToHeader getToHeader() throws SipParseException {
        return toHeader != null ? toHeader.ensure().toToHeader() : null;
    }

    @Override
    public ViaHeader getViaHeader() throws SipParseException {
        return viaHeader != null ? viaHeader.ensure().toViaHeader() : null;
    }

    @Override
    public List<ViaHeader> getViaHeaders() throws SipParseException {

        final List<SipHeader> headerValues = headers.get(ViaHeader.NAME.toString());
        if(headerValues == null || headerValues.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ViaHeader> vias = new ArrayList<>(headerValues.size());
        for(final SipHeader via : headerValues) {
            vias.add(via.ensure().toViaHeader());
        }

        return vias;
    }

    @Override
    public MaxForwardsHeader getMaxForwards() throws SipParseException {
        return maxForwardsHeader != null ? maxForwardsHeader.ensure().toMaxForwardsHeader() : null;
    }

    @Override
    public RecordRouteHeader getRecordRouteHeader() throws SipParseException {
        return recordRouteHeader != null ? recordRouteHeader.ensure().toRecordRouteHeader() : null;
    }

    @Override
    public List<RecordRouteHeader> getRecordRouteHeaders() throws SipParseException {

        final List<SipHeader> headerValues = headers.get(RecordRouteHeader.NAME.toString());
        if(headerValues == null || headerValues.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RecordRouteHeader> routes = new ArrayList<>(headerValues.size());
        for(final SipHeader route : headerValues) {
            routes.add(route.ensure().toRecordRouteHeader());
        }

        return routes;
    }

    @Override
    public RouteHeader getRouteHeader() throws SipParseException {
        return routeHeader != null ? routeHeader.ensure().toRouteHeader() : null;
    }

    @Override
    public List<RouteHeader> getRouteHeaders() throws SipParseException {
        final List<SipHeader> headerValues = headers.get(RouteHeader.NAME.toString());
        if(headerValues == null || headerValues.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RouteHeader> routes = new ArrayList<>(headerValues.size());
        for(final SipHeader route : headerValues) {
            routes.add(route.ensure().toRouteHeader());
        }

        return routes;
    }

    @Override
    public ExpiresHeader getExpiresHeader() throws SipParseException {
        final SipHeader header = findHeader(ExpiresHeader.NAME.toString());
        return header != null ? header.ensure().toExpiresHeader() : null;
    }

    @Override
    public ContactHeader getContactHeader() throws SipParseException {
        return contactHeader != null ? contactHeader.ensure().toContactHeader() : null;
    }

    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipParseException {
        final SipHeader header = findHeader(ContentTypeHeader.NAME.toString());
        return header != null ? header.ensure().toContentTypeHeader() : null;
    }

    @Override
    public int getContentLength() throws SipParseException {
        final SipHeader header = findHeader(ContentLengthHeader.NAME.toString());
        return header != null ? header.ensure().toContentLengthHeader().getContentLength() : 0;
    }

    @Override
    public CallIdHeader getCallIDHeader() throws SipParseException {
        return callIdHeader != null ? callIdHeader.ensure().toCallIdHeader() : null;
    }

    @Override
    public CSeqHeader getCSeqHeader() throws SipParseException {
        return cSeqHeader != null ? cSeqHeader.ensure().toCSeqHeader() : null;
    }

    @Override
    public boolean isInitial() throws SipParseException {
        final ToHeader to = getToHeader();
        return to.getTag() == null;
    }

    @Override
    public void verify() {

    }

    private SipHeader findHeader(final String name) {
        final List<SipHeader> headerValues = headers.get(name);
        return headerValues != null && !headerValues.isEmpty() ? headerValues.get(0) :null;
    }


    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (o instanceof SipMessage) {
            final SipMessage other = (SipMessage) o;
            return initialLine.equals(other.initialLine());
        }

        return false;
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
