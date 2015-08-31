package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageBuilder implements SipMessageImpl.Builder<SipMessage> {

    private final SipMessage template;
    private final List<SipHeader> headers;

    private Predicate<SipHeader> filter;

    private CSeqHeader cseq;
    private CSeqHeader.Builder cseqBuilder;

    private MaxForwardsHeader maxForwards;
    private MaxForwardsHeader.Builder maxForwardsBuilder;
    private Function<MaxForwardsHeader, MaxForwardsHeader.Builder> onMaxForwards;
    private Consumer<MaxForwardsHeader.Builder> onMaxForwardsBuilder;

    private short indexOfTo = -1;
    private short indexOfFrom = -1;
    private short indexOfCSeq = -1;
    private short indexOfCallId = -1;
    private short indexOfMaxForwards = -1;
    private short indexOfVia = -1;
    private short indexOfRoute = -1;
    private short indexOfRecordRoute = -1;
    private short indexOfContact = -1;

    public SipMessageBuilder(final SipMessage template) {
        this.template = template;
        // most common is that when a message is proxied or b2bua:ed
        // that we add more headers to the message. We really want to avoid
        // to expand the list.
        // TODO: grab real-world traffic and do some analysis
        headers = new ArrayList<>(template.countNoOfHeaders() + 5);
    }

    @Override
    public SipMessage.Builder withNoDefaults() {
        return this;
    }

    @Override
    public SipMessage.Builder filter(final Predicate<SipHeader> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public SipMessage.Builder onHeader(final Function<SipHeader, Optional<SipHeader.Builder>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder onHeaderBuilder(final Consumer<SipHeader.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withHeader(final SipHeader header) {
        return this;
    }

    @Override
    public SipMessage.Builder withHeader(final SipHeader.Builder<SipHeader> header) {
        return this;
    }

    @Override
    public SipMessage.Builder onFromHeader(final Function<FromHeader, Optional<AddressParametersHeader.Builder<FromHeader>>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder onFromHeaderBuilder(final Consumer<AddressParametersHeader.Builder<FromHeader>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withFromHeader(final FromHeader from) {
        return this;
    }

    @Override
    public SipMessage.Builder withFromHeader(final AddressParametersHeader.Builder<FromHeader> builder) {
        return this;
    }

    @Override
    public SipMessage.Builder onToHeader(final Function<ToHeader, Optional<AddressParametersHeader.Builder<ToHeader>>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder onToHeaderBuilder(final Consumer<AddressParametersHeader.Builder<ToHeader>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withToHeader(final ToHeader to) {
        return this;
    }

    @Override
    public SipMessage.Builder withToHeader(final AddressParametersHeader.Builder<ToHeader> builder) {
        return this;
    }

    @Override
    public SipMessage.Builder onContactHeader(final Function<ContactHeader, Optional<AddressParametersHeader.Builder<ContactHeader>>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder onContactHeaderBuilder(final Consumer<AddressParametersHeader.Builder<ContactHeader>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withContactHeader(final ContactHeader contact) {
        return this;
    }

    @Override
    public SipMessage.Builder withContactHeader(final AddressParametersHeader.Builder<ContactHeader> builder) {
        return this;
    }

    @Override
    public SipMessage.Builder onCSeqHeader(final Function<CSeqHeader, CSeqHeader.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder onCSeqHeaderBuilder(final Consumer<CSeqHeader.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withCSeqHeader(final CSeqHeader cseq) {
        this.cseq = cseq;
        return null;
    }

    @Override
    public SipMessage.Builder withCSeqHeader(final CSeqHeader.Builder builder) {
        this.cseqBuilder = builder;
        return null;
    }

    @Override
    public SipMessage.Builder onMaxForwardsHeader(final Function<MaxForwardsHeader, MaxForwardsHeader.Builder> f) {
        this.onMaxForwards = f;
        return this;
    }

    @Override
    public SipMessage.Builder onMaxForwardsHeaderBuilder(final Consumer<MaxForwardsHeader.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withMaxForwardsHeader(final MaxForwardsHeader maxForwards) {
        return this;
    }

    @Override
    public SipMessage.Builder withMaxForwardsHeader(final MaxForwardsHeader.Builder builder) {
        return this;
    }

    @Override
    public SipMessage.Builder onRequestURI(final Function<SipURI, SipURI.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder onRequestURIBuilder(final Consumer<SipURI.Builder> f) {
        return this;
    }

    @Override
    public SipMessage build() {
        int msgSize = 2;
        short index = 0;
        if (template != null) {
            for (final SipHeader header : template.getAllHeaders()) {
                if (filter == null || filter.test(header)) {
                    final SipHeader finalHeader = processHeader(index, header);
                    if (finalHeader != null) {
                        msgSize += finalHeader.getBufferSize() + 2;
                        headers.add(finalHeader);
                        ++index;
                    }
                }
            }
        }

        // TODO: we should adjust the content length automatically
        // if there is a body (or no body). Or should we? What if you
        // want to create a message with a bad Content Length?


        // TODO: not correct but will do for now...
        final SipInitialLine initialLine = template.initialLine();
        final Buffer initialLineBuffer = initialLine.getBuffer();
        msgSize += initialLineBuffer.capacity() + 2;

        // TODO: instead of copying over the bytes like this create
        // a composite buffer...
        final Buffer msg = Buffers.createBuffer(msgSize);

        initialLineBuffer.getBytes(msg);
        msg.write(SipParser.CR);
        msg.write(SipParser.LF);

        for (final SipHeader header : headers) {
            header.getBytes(msg);
            msg.write(SipParser.CR);
            msg.write(SipParser.LF);
        }

        msg.write(SipParser.CR);
        msg.write(SipParser.LF);

        final Buffer body = template.getRawContent();
        if (body != null) {
            body.getBytes(msg);
        }

        if (initialLine.isRequestLine()) {
            return new ImmutableSipRequest(msg, initialLine.toRequestLine(), headers,
                    indexOfTo,
                    indexOfFrom,
                    indexOfCSeq,
                    indexOfCallId,
                    indexOfMaxForwards,
                    indexOfVia,
                    indexOfRoute,
                    indexOfRecordRoute,
                    indexOfContact,
                    body);
        } else {
            throw new RuntimeException("haven't implemented the SipResponse just yet");
        }
    }

    private SipHeader processHeader(final short index, final SipHeader header) {
        SipHeader finalHeader = header;

        if (header.isContactHeader() && indexOfContact == -1) {
            finalHeader = header.ensure().toContactHeader();
            indexOfContact = index;
        } else if (header.isCSeqHeader() && indexOfCSeq == -1) {
            finalHeader = header.ensure().toCSeqHeader();
            indexOfCSeq = index;
        } else if (header.isMaxForwardsHeader() && indexOfMaxForwards == -1) {
            indexOfMaxForwards = index;
            return processMaxForwards(header);
        } else if (header.isFromHeader() && indexOfFrom == -1) {
            finalHeader = header.ensure().toFromHeader();
            indexOfFrom = index;
        } else if (header.isToHeader() && indexOfTo == -1) {
            finalHeader = header.ensure().toToHeader();
            indexOfTo = index;
        } else if (header.isViaHeader() && indexOfVia == -1) {
            finalHeader = header.ensure().toViaHeader();
            indexOfVia = index;
        } else if (header.isCallIdHeader() && indexOfCallId == -1) {
            finalHeader = header.ensure().toCallIdHeader();
            indexOfCallId = index;
        } else if (header.isRouteHeader() && indexOfRoute == -1) {
            finalHeader = header.ensure().toRouterHeader();
            indexOfRoute = index;
        } else if (header.isRecordRouteHeader() && indexOfRecordRoute == -1) {
            finalHeader = header.ensure().toRecordRouteHeader();
            indexOfRecordRoute = index;
        }

        // TODO: need to check if there is a onHeader function and then
        // invoke it.
        return finalHeader;
    }


    private MaxForwardsHeader processMaxForwards(final SipHeader header) {
        MaxForwardsHeader max = null;
        if (maxForwardsBuilder != null) {
            if (onMaxForwardsBuilder != null) {
                onMaxForwardsBuilder.accept(maxForwardsBuilder);
            }
            max = maxForwardsBuilder.build();
        } else if (maxForwards != null) {
            max = invokeMaxForwardsFunction(maxForwards);
        } else if (template != null) {
            max = invokeMaxForwardsFunction(template.getMaxForwards());
        }

        return max;
    }

    private MaxForwardsHeader invokeMaxForwardsFunction(final MaxForwardsHeader max) {
        if (onMaxForwards != null) {
            final MaxForwardsHeader.Builder b = onMaxForwards.apply(max);
            if (b != null) {
                return b.build();
            }
        }
        return max;
    }

    @Override
    public void onCommit(final Consumer<SipMessage> f) {

    }
}
