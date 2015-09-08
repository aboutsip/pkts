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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageBuilder implements SipMessageImpl.Builder<SipMessage> {

    private final SipMessage template;

    // private final List<SipHeaderProducer> headers;
    private final List<SipHeader> headers;

    private Predicate<SipHeader> filter;

    private CSeqHeader cseq;
    private CSeqHeader.Builder cseqBuilder;

    private MaxForwardsHeader maxForwards;
    private Consumer<MaxForwardsHeader.Builder> onMaxForwardsBuilder;

    private ToHeader toHeader;
    private Consumer<AddressParametersHeader.Builder<ToHeader>> onToBuilder;

    private FromHeader fromHeader;
    private Consumer<AddressParametersHeader.Builder<FromHeader>> onFromBuilder;

    private Function<SipHeader, SipHeader> onHeaderFunction;

    private short indexOfTo = -1;
    private short indexOfFrom = -1;
    private short indexOfCSeq = -1;
    private short indexOfCallId = -1;
    private short indexOfMaxForwards = -1;
    private short indexOfVia = -1;
    private short indexOfRoute = -1;
    private short indexOfRecordRoute = -1;
    private short indexOfContact = -1;

    /**
     * By default, this builder will add certain headers if missing
     * but if the user wish to turn off this behavior then she can
     * do so by flipping this flag.
     */
    private boolean useDefaults = true;

    public SipMessageBuilder(final SipMessage template) {
        this.template = template;
        headers = new ArrayList<>(10);
    }

    @Override
    public SipMessage.Builder withNoDefaults() {
        useDefaults = false;
        return this;
    }

    /*
    @Override
    public SipMessage.Builder filter(final Predicate<SipHeader> filter) throws IllegalStateException {
        if (this.filter != null) {
            throw new IllegalStateException("A filter has already been specified");
        }
        this.filter = filter;
        return this;
    }
    */

    /*
    @Override
    public SipMessage.Builder modify(final Predicate<SipHeader> filter) {
        this.filter = filter;
        return this;
    }
    */

    @Override
    public SipMessage.Builder onHeader(final Function<SipHeader, SipHeader> f) throws IllegalStateException {
        if (this.onHeaderFunction == null) {
            this.onHeaderFunction = f;
        } else {
            this.onHeaderFunction = this.onHeaderFunction.andThen(f);
        }
        return this;
    }

    @Override
    public SipMessage.Builder withHeader(final SipHeader header) {
        if (header != null) {
            if (header.isContactHeader()) {
                // TODO
            } else if (header.isCSeqHeader()) {
                // TODO
            } else if (header.isMaxForwardsHeader()) {
                // TODO
            } else if (header.isFromHeader()) {
                this.fromHeader = header.ensure().toFromHeader();
            } else if (header.isToHeader()) {
                this.toHeader = header.ensure().toToHeader();
            } else if (header.isViaHeader()) {
                // TODO
            } else if (header.isCallIdHeader()) {
                // TODO
            } else if (header.isRouteHeader()) {
                // TODO
            } else if (header.isRecordRouteHeader()) {
                // TODO
            } else {
                headers.add(header);
            }
        }
        return this;
    }

    @Override
    public SipMessage.Builder withPushHeader(SipHeader header) {
        return this;
    }

    @Override
    public SipMessage.Builder onFromHeader(final Consumer<AddressParametersHeader.Builder<FromHeader>> f) {
        if (this.onFromBuilder != null) {
            this.onFromBuilder = this.onFromBuilder.andThen(f);
        } else {
            this.onFromBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder withFromHeader(final FromHeader from) {
        this.fromHeader = from;
        return this;
    }

    @Override
    public SipMessage.Builder onToHeader(final Consumer<AddressParametersHeader.Builder<ToHeader>> f) {
        if (this.onToBuilder != null) {
            this.onToBuilder = this.onToBuilder.andThen(f);
        } else {
            this.onToBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder withToHeader(final ToHeader to) {
        this.toHeader = to;
        return this;
    }

    @Override
    public SipMessage.Builder onContactHeader(final Consumer<AddressParametersHeader.Builder<ContactHeader>> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withContactHeader(final ContactHeader contact) {
        return this;
    }

    @Override
    public SipMessage.Builder onCSeqHeader(final Consumer<CSeqHeader.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder withCSeqHeader(final CSeqHeader cseq) {
        this.cseq = cseq;
        return null;
    }

    @Override
    public SipMessage.Builder onMaxForwardsHeader(final Consumer<MaxForwardsHeader.Builder> f) {
        if (this.onMaxForwardsBuilder != null) {
            this.onMaxForwardsBuilder.andThen(f);
        } else {
            this.onMaxForwardsBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder withMaxForwardsHeader(final MaxForwardsHeader maxForwards) {
        return this;
    }

    @Override
    public SipMessage.Builder onRequestURI(final Function<SipURI, SipURI> f) {
        return this;
    }

    @Override
    public SipMessage build() {
        int msgSize = 2;
        short index = 0;

        // TODO: run some analysis on what the average number of headers in a message
        // is.
        final List<SipHeader> finalHeaders
                = new ArrayList<>(template != null ? template.getAllHeaders().size() + 10 : 20);

        if (template != null) {
            for (final SipHeader header : template.getAllHeaders()) {
                final SipHeader finalHeader = processHeader(index, header);
                if (finalHeader != null) {
                    msgSize += finalHeader.getBufferSize() + 2;
                    finalHeaders.add(finalHeader);
                    ++index;
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

        for (final SipHeader header : finalHeaders) {
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
            return new ImmutableSipRequest(msg, initialLine.toRequestLine(), finalHeaders,
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
            final MaxForwardsHeader max = maxForwards != null ? maxForwards : header.ensure().toMaxForwardsHeader();
            finalHeader = invokeMaxForwardsFunction(max);
        } else if (header.isFromHeader() && indexOfFrom == -1) {
            indexOfFrom = index;
            final FromHeader from = fromHeader != null ? fromHeader : header.ensure().toFromHeader();
            finalHeader = invokeFromHeaderFunction(from);
        } else if (header.isToHeader() && indexOfTo == -1) {
            indexOfTo = index;
            final ToHeader to = toHeader != null ? toHeader : header.ensure().toToHeader();
            finalHeader = invokeToHeaderFunction(to);
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
        } else {
            finalHeader = processGenericHeader(header);
        }

        return finalHeader;
    }

    private SipHeader processGenericHeader(final SipHeader header) {
        if (this.onHeaderFunction != null) {
            return this.onHeaderFunction.apply(header);
        }

        return header;
    }

    private FromHeader processFromHeader(final FromHeader header) {
        return null;
    }

    private MaxForwardsHeader processMaxForwards(final MaxForwardsHeader header) {
        MaxForwardsHeader max = null;
        if (maxForwards != null) {
            max = invokeMaxForwardsFunction(maxForwards);
        } else if (template != null) {
            max = invokeMaxForwardsFunction(header);
        }

        return max;
    }

    private ToHeader invokeToHeaderFunction(final ToHeader to) {
        if (to != null && onToBuilder != null) {
            final AddressParametersHeader.Builder<ToHeader> b = to.copy();
            onToBuilder.accept(b);
            return b.build();
        }
        return to;
    }

    private FromHeader invokeFromHeaderFunction(final FromHeader from) {
        if (from != null && onFromBuilder != null) {
            final AddressParametersHeader.Builder<FromHeader> b = from.copy();
            onFromBuilder.accept(b);
            return b.build();
        }
        return from;
    }

    private MaxForwardsHeader invokeMaxForwardsFunction(final MaxForwardsHeader max) {
        if (max != null && onMaxForwardsBuilder != null) {
            final MaxForwardsHeader.Builder b = max.copy();
            onMaxForwardsBuilder.accept(b);
            return b.build();
        }
        return max;
    }

    @Override
    public SipMessage.Builder onCommit(final Consumer<SipMessage> f) {
        return this;
    }

    private interface SipHeaderProducer {
        SipHeader onHeader(Function<SipHeader, SipHeader.Builder> f, Consumer<SipHeader.Builder> f2);

        static SipHeaderProducer create(final SipHeader header) {
            return new SipHeaderWrapper(header);
        }

        static SipHeaderProducer create(final SipHeader.Builder builder) {
            return new SipHeaderBuilderWrapper(builder);
        }
    }

    private static class SipHeaderWrapper implements SipHeaderProducer {

        private final SipHeader header;

        private SipHeaderWrapper(final SipHeader header) {
            this.header = header;
        }

        @Override
        public SipHeader onHeader(final Function<SipHeader, SipHeader.Builder> f, final Consumer<SipHeader.Builder> f2) {
            if (f != null) {
                final SipHeader.Builder builder = f.apply(header);
                if (builder != null) {
                    return builder.build();
                }
            }
            return header;
        }
    }

    private static class SipHeaderBuilderWrapper implements SipHeaderProducer {

        private final SipHeader.Builder builder;

        private SipHeaderBuilderWrapper(final SipHeader.Builder builder) {
            this.builder = builder;
        }

        @Override
        public SipHeader onHeader(final Function<SipHeader, SipHeader.Builder> f, final Consumer<SipHeader.Builder> f2) {
            if (f2 != null) {
                f2.accept(builder);
            }
            return builder.build();
        }
    }

}
