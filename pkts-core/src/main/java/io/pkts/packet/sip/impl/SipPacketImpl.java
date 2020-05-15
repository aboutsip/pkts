/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.impl.AbstractPacket;
import io.pkts.packet.impl.SDPPacketImpl;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipPacketParseException;
import io.pkts.packet.sip.SipRequestPacket;
import io.pkts.packet.sip.SipResponsePacket;
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
import io.pkts.protocol.Protocol;
import io.pkts.sdp.SDP;
import io.pkts.sdp.SDPFactory;
import io.pkts.sdp.SdpException;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class SipPacketImpl extends AbstractPacket implements SipPacket {
    /**
     * The actual SIP message. The {@link SipPacket} is merely a thin wrapper
     * around this object in order to make if fit the pcap model whereas the
     * actual {@link SipMessage} is a pure SIP object only.
     */
    private final SipMessage msg;

    /**
     * 
     */
    public SipPacketImpl(final TransportPacket parent, final SipMessage msg) {
        super(Protocol.SIP, parent, null);
        this.msg = msg;
    }

    protected TransportPacket getTransportPacket() {
        return (TransportPacket) getParentPacket();
    }

    protected SipMessage getSipMessage() {
        return this.msg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getArrivalTime()
     */
    @Override
    public long getArrivalTime() {
        return getTransportPacket().getArrivalTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#write(java.io.OutputStream)
     */
    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        this.getParentPacket().write(out, Buffers.wrap(this.msg.toBuffer(), payload));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getInitialLine()
     */
    @Override
    public Buffer getInitialLine() {
        return this.msg.getInitialLine();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toRequest()
     */
    @Override
    public SipRequestPacket toRequest() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toResponse()
     */
    @Override
    public SipResponsePacket toResponse() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipResponse");
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isResponse()
     */
    @Override
    public boolean isResponse() {
        return this.msg.isResponse();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isRequest()
     */
    @Override
    public boolean isRequest() {
        return this.msg.isRequest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContent()
     */
    @Override
    public Object getContent() throws SipPacketParseException {
        return parseSipContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContent()
     */
    @Override
    public Buffer getRawContent() {
        return this.msg.getContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#hasContent()
     */
    @Override
    public boolean hasContent() {
        return this.msg.hasContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getMethod()
     */
    @Override
    public Buffer getMethod() throws SipPacketParseException {
        return this.msg.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SipHeader> getHeaders() {
        return this.msg.getAllHeaders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<SipHeader>> getHeaderValues() {
        return this.msg.getHeaderValues();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getHeader(io.pkts.buffer.Buffer)
     */
    @Override
    public Optional<SipHeader> getHeader(final Buffer headerName) throws SipPacketParseException {
        return this.msg.getHeader(headerName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getHeader(java.lang.String)
     */
    @Override
    public Optional<SipHeader> getHeader(final String headerName) throws SipPacketParseException {
        return this.msg.getHeader(headerName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getFromHeader()
     */
    @Override
    public FromHeader getFromHeader() throws SipPacketParseException {
        return this.msg.getFromHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getToHeader()
     */
    @Override
    public ToHeader getToHeader() throws SipPacketParseException {
        return this.msg.getToHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getViaHeader()
     */
    @Override
    public ViaHeader getViaHeader() throws SipPacketParseException {
        return this.msg.getViaHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getViaHeaders()
     */
    @Override
    public List<ViaHeader> getViaHeaders() throws SipPacketParseException {
        return this.msg.getViaHeaders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getMaxForwards()
     */
    @Override
    public MaxForwardsHeader getMaxForwards() throws SipPacketParseException {
        return this.msg.getMaxForwards();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRecordRouteHeader()
     */
    @Override
    public RecordRouteHeader getRecordRouteHeader() throws SipPacketParseException {
        return this.msg.getRecordRouteHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRecordRouteHeaders()
     */
    @Override
    public List<RecordRouteHeader> getRecordRouteHeaders() throws SipPacketParseException {
        return this.msg.getRecordRouteHeaders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRouteHeader()
     */
    @Override
    public RouteHeader getRouteHeader() throws SipPacketParseException {
        return this.msg.getRouteHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRouteHeaders()
     */
    @Override
    public List<RouteHeader> getRouteHeaders() throws SipPacketParseException {
        return this.msg.getRouteHeaders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContactHeader()
     */
    @Override
    public ContactHeader getContactHeader() throws SipPacketParseException {
        return this.msg.getContactHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContentTypeHeader()
     */
    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipPacketParseException {
        return this.msg.getContentTypeHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getCallIDHeader()
     */
    @Override
    public CallIdHeader getCallIDHeader() throws SipPacketParseException {
        return this.msg.getCallIDHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getCSeqHeader()
     */
    @Override
    public CSeqHeader getCSeqHeader() throws SipPacketParseException {
        return this.msg.getCSeqHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isInvite()
     */
    @Override
    public boolean isInvite() throws SipPacketParseException {
        return this.msg.isInvite();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isBye()
     */
    @Override
    public boolean isBye() throws SipPacketParseException {
        return this.msg.isBye();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isAck()
     */
    @Override
    public boolean isAck() throws SipPacketParseException {
        return this.msg.isAck();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isOptions()
     */
    @Override
    public boolean isOptions() throws SipPacketParseException {
        return this.msg.isOptions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isMessage()
     */
    @Override
    public boolean isMessage() throws SipPacketParseException {
        return this.msg.isMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isInfo()
     */
    @Override
    public boolean isInfo() throws SipPacketParseException {
        return this.msg.isInfo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isCancel()
     */
    @Override
    public boolean isCancel() throws SipPacketParseException {
        return this.msg.isCancel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isInitial()
     */
    @Override
    public boolean isInitial() throws SipPacketParseException {
        return this.msg.isInitial();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#verify()
     */
    @Override
    public void verify() {
        this.msg.verify();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toBuffer()
     */
    @Override
    public Buffer toBuffer() {
        return this.msg.toBuffer();
    }

    @Override
    public abstract SipPacket clone();

    private Object parseSipContent() {
        try {
            final Buffer content = this.msg.getContent();
            final ContentTypeHeader contentType = getContentTypeHeader();
              if (content != null && !content.isEmpty() && contentType.isSDP()) {
                return SDPFactory.getInstance().parse(content);
            }
        } catch (final SipPacketParseException | SdpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getNextPacket()
     */
    @Override
    public Packet getNextPacket() throws IOException {
        final Object content = parseSipContent();
        if (content instanceof SDP) {
            return new SDPPacketImpl(this, (SDP)content);
        }
        return null;
    }

    @Override
    public Buffer getPayload() {
        return this.msg.getContent();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (getTransportPacket().isUDP()) {
            sb.append("U ");
        } else if (getTransportPacket().isTCP()) {
            sb.append("T ");
        } else {
            // TODO: need WS, SCTP etc as well. but not as common
            // right now so no big deal.
        }

        // final DateTimeFormatter formatter =
        // DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS");
        final Instant timestamp = Instant.ofEpochMilli(getArrivalTime() / 1000);
        IPPacket ipPacket = (IPPacket) getParentPacket().getParentPacket();
        sb.append(timestamp.toString())
          .append(" ").append(ipPacket.getSourceIP()).append(":").append(getTransportPacket().getSourcePort())
          .append(" -> ").append(ipPacket.getDestinationIP()).append(":").append(getTransportPacket().getDestinationPort())
          .append("\n")
          .append(this.msg.toString());
        return sb.toString();
    }
}
