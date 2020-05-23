package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentLengthHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class SipMessageBuilder<T extends SipMessage> implements SipMessage.Builder<T> {

    /**
     * These are all the headers that the user has added to this builder.
     * These headers may have been added to this list through any of the
     * withXXX-methods or they could have been copied from the
     * template if one was used.
     */
    private final List<SipHeader> headers;

    /**
     * All headers added to this builder is subject
     * to filtering.
     */
    private Predicate<SipHeader> filter;

    private Function<SipURI, SipURI> onRequestURIFunction;

    private CSeqHeader cseq;
    private CSeqHeader.Builder cseqBuilder;

    private Consumer<MaxForwardsHeader.Builder> onMaxForwardsBuilder;

    private Consumer<AddressParametersHeader.Builder<ToHeader>> onToBuilder;

    private Consumer<AddressParametersHeader.Builder<FromHeader>> onFromBuilder;

    private Consumer<AddressParametersHeader.Builder<ContactHeader>> onContactBuilder;

    private List<ViaHeader> viaHeaders;
    private Consumer<ViaHeader.Builder> onTopMostViaBuilder;
    private BiConsumer<Integer, ViaHeader.Builder> onViaBuilder;

    private List<RecordRouteHeader> recordRouteHeaders;
    private Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> onTopMostRecordRouteBuilder;
    private Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> onRecordRouteBuilder;

    private List<RouteHeader> routeHeaders;
    private Consumer<AddressParametersHeader.Builder<RouteHeader>> onTopMostRouteBuilder;
    private Consumer<AddressParametersHeader.Builder<RouteHeader>> onRouteBuilder;

    private Function<SipHeader, SipHeader> onHeaderFunction;

    /**
     * TODO: should probably allow to pass in an object as well.
     */
    private Buffer body;

    private SipHeader toHeader;
    private SipHeader fromHeader;
    private SipHeader cSeqHeader;
    private SipHeader callIdHeader;
    private SipHeader maxForwardsHeader;
    private SipHeader viaHeader;
    private SipHeader routeHeader;
    private SipHeader recordRouteHeader;
    private SipHeader contactHeader;

    /**
     * By default, this builder will add certain headers if missing
     * but if the user wish to turn off this behavior then she can
     * do so by flipping this flag.
     */
    private boolean useDefaults = true;

    protected SipMessageBuilder(final int headerSizeHint) {
        headers = new ArrayList<>(headerSizeHint);
    }

    protected SipMessageBuilder() {
        this(15);
    }

    @Override
    public SipMessage.Builder<T> withNoDefaults() {
        useDefaults = false;
        return this;
    }

    @Override
    public SipMessage.Builder<T> onHeader(final Function<SipHeader, SipHeader> f) throws IllegalStateException {
        if (this.onHeaderFunction == null) {
            this.onHeaderFunction = f;
        } else {
            this.onHeaderFunction = this.onHeaderFunction.andThen(f);
        }
        return this;
    }

    private void processHeader(final SipHeader header) {
        if (header.isContactHeader()) {
            addHeader(header);
            contactHeader = header;
        } else if (header.isCSeqHeader()) {
            addHeader(header);
            cSeqHeader = header;
        } else if (header.isMaxForwardsHeader()) {
            addHeader(header);
            maxForwardsHeader = header;
        } else if (header.isFromHeader()) {
            addHeader(header);
            fromHeader = header;
        } else if (header.isToHeader()) {
            addHeader(header);
            toHeader = header;
        } else if (header.isViaHeader()) {
            viaHeaders = ensureList(viaHeaders);
            viaHeaders.add(header.ensure().toViaHeader());
            viaHeader = header;
        } else if (header.isCallIdHeader()) {
            addHeader(header);
            callIdHeader = header;
        } else if (header.isRouteHeader()) {
            routeHeaders = ensureList(routeHeaders);
            routeHeaders.add(header.ensure().toRouteHeader());
            routeHeader = header;
        } else if (header.isRecordRouteHeader()) {
            recordRouteHeaders = ensureList(recordRouteHeaders);
            recordRouteHeaders.add(header.ensure().toRecordRouteHeader());
            recordRouteHeader = header;
        } else {
            addHeader(header);
        }
    }

    private short addHeader(final SipHeader header) {
        headers.add(header);
        return (short)(headers.size() - 1);
    }

    @Override
    public SipMessage.Builder<T> withHeader(final SipHeader header) {
        if (header != null) {
            processHeader(header);
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withHeaders(final List<SipHeader> headers) {
        if (headers != null) {
            headers.forEach(this::processHeader);
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withPushHeader(final SipHeader header) {
        return this;
    }

    @Override
    public SipMessage.Builder<T> onFromHeader(final Consumer<AddressParametersHeader.Builder<FromHeader>> f) {
        if (this.onFromBuilder != null) {
            this.onFromBuilder = this.onFromBuilder.andThen(f);
        } else {
            this.onFromBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withFromHeader(final FromHeader from) {
        if (from != null) {
            addHeader(from);
            fromHeader = from;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withFromHeader(final String from) {
        return withFromHeader(FromHeader.frame(Buffers.wrap(from)));
    }

    @Override
    public SipMessage.Builder<T> onToHeader(final Consumer<AddressParametersHeader.Builder<ToHeader>> f) {
        if (this.onToBuilder != null) {
            this.onToBuilder = this.onToBuilder.andThen(f);
        } else {
            this.onToBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withToHeader(final ToHeader to) {
        if (to != null) {
            addHeader(to);
            toHeader = to;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withToHeader(final String to) {
        return withToHeader(ToHeader.frame(Buffers.wrap(to)));
    }

    @Override
    public SipMessage.Builder<T> onContactHeader(final Consumer<AddressParametersHeader.Builder<ContactHeader>> f) {
        if (this.onContactBuilder != null) {
            this.onContactBuilder = this.onContactBuilder.andThen(f);
        } else {
            this.onContactBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withContactHeader(final ContactHeader contact) {
        if (contact != null) {
            addHeader(contact);
            contactHeader = contact;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> onCSeqHeader(final Consumer<CSeqHeader.Builder> f) {
        return this;
    }

    @Override
    public SipMessage.Builder<T> withCSeqHeader(final CSeqHeader cseq) {
        if (cseq != null) {
            addHeader(cseq);
            cSeqHeader = cseq;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withCallIdHeader(final CallIdHeader callID) {
        if (callID != null) {
            addHeader(callID);
            callIdHeader = callID;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> onMaxForwardsHeader(final Consumer<MaxForwardsHeader.Builder> f) {
        if (this.onMaxForwardsBuilder != null) {
            this.onMaxForwardsBuilder.andThen(f);
        } else {
            this.onMaxForwardsBuilder = f;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withMaxForwardsHeader(final MaxForwardsHeader maxForwards) {
        addHeader(maxForwards);
        maxForwardsHeader = maxForwards;
        return this;
    }

    @Override
    public SipMessage.Builder<T> onTopMostViaHeader(final Consumer<ViaHeader.Builder> f) {
        this.onTopMostViaBuilder = chainConsumers(this.onTopMostViaBuilder, f);
        return this;
    }

    @Override
    public SipMessage.Builder<T> onViaHeader(final BiConsumer<Integer, ViaHeader.Builder> f) {
        this.onViaBuilder = chainConsumers(this.onViaBuilder, f);
        return this;
    }

    @Override
    public SipMessage.Builder<T> onTopMostRouteHeader(final Consumer<AddressParametersHeader.Builder<RouteHeader>> f) {
        this.onTopMostRouteBuilder = chainConsumers(this.onTopMostRouteBuilder, f);
        return this;
    }

    @Override
    public SipMessage.Builder<T> onRouteHeader(final Consumer<AddressParametersHeader.Builder<RouteHeader>> f) {
        this.onRouteBuilder = chainConsumers(this.onRouteBuilder, f);
        return this;
    }

    @Override
    public SipMessage.Builder<T> withRouteHeader(final RouteHeader route) {
        if (route != null) {
            this.routeHeaders = ensureList(this.routeHeaders);
            this.routeHeaders.clear();
            this.routeHeaders.add(route);
        }

        return this;
    }

    @Override
    public SipMessage.Builder<T> withRouteHeaders(final RouteHeader... routes) {
        if (routes != null && routes.length > 0) {
            this.routeHeaders = ensureList(this.routeHeaders);
            this.routeHeaders.clear();
            routeHeader = routes[0];
            this.routeHeaders.addAll(Arrays.asList(routes));
        }

        return this;
    }

    @Override
    public SipMessage.Builder<T> withRouteHeaders(final List<RouteHeader> routes) {
        if (routes != null && !routes.isEmpty()) {
            this.routeHeaders = ensureList(this.routeHeaders);
            this.routeHeaders.clear();
            routeHeader = routes.get(0);
            this.routeHeaders.addAll(routes);
        }

        return this;
    }

    @Override
    public SipMessage.Builder<T> withTopMostRouteHeader(final RouteHeader route) {
        if (route != null) {
            this.routeHeaders = ensureList(this.routeHeaders);
            this.routeHeaders.add(0, route);
            routeHeader = route;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withPoppedRoute() {
        if (this.routeHeaders != null) {
            this.routeHeaders.remove(0);
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withNoRoutes() {
        if (this.routeHeaders != null) {
            this.routeHeaders.clear();
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> onTopMostRecordRouteHeader(final Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> f) {
        this.onTopMostRecordRouteBuilder = chainConsumers(this.onTopMostRecordRouteBuilder, f);
        return this;
    }

    @Override
    public SipMessage.Builder<T> onRecordRouteHeader(final Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> f) {
        this.onRecordRouteBuilder = chainConsumers(this.onRecordRouteBuilder, f);
        return this;
    }

    @Override
    public SipMessage.Builder<T> withRecordRouteHeader(final RecordRouteHeader recordRoute) {
        if (recordRoute != null) {
            this.recordRouteHeaders = ensureList(this.recordRouteHeaders);
            this.recordRouteHeaders.clear();
            this.recordRouteHeaders.add(recordRoute);
            recordRouteHeader = recordRoute;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withRecordRouteHeaders(final RecordRouteHeader... recordRoutes) {
        if (recordRoutes != null && recordRoutes.length > 0) {
            this.recordRouteHeaders = ensureList(this.recordRouteHeaders);
            this.recordRouteHeaders.clear();
            recordRouteHeader = recordRoutes[0];
            this.recordRouteHeaders.addAll(Arrays.asList(recordRoutes));
        }

        return this;
    }

    @Override
    public SipMessage.Builder<T> withRecordRouteHeaders(final List<RecordRouteHeader> recordRoutes) {
        if (recordRoutes != null && !recordRoutes.isEmpty()) {
            this.recordRouteHeaders = ensureList(this.recordRouteHeaders);
            this.recordRouteHeaders.clear();
            recordRouteHeader = recordRoutes.get(0);
            this.recordRouteHeaders.addAll(recordRoutes);
        }

        return this;
    }

    @Override
    public SipMessage.Builder<T> withTopMostRecordRouteHeader(final RecordRouteHeader recordRoute) {
        if (recordRoute != null) {
            this.recordRouteHeaders = ensureList(this.recordRouteHeaders);
            this.recordRouteHeaders.add(0, recordRoute);
            recordRouteHeader = recordRoute;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withViaHeader(final ViaHeader via) {
        if (via != null) {
            this.viaHeaders = ensureList(this.viaHeaders);
            this.viaHeaders.clear();
            this.viaHeaders.add(via);
            viaHeader = via;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withViaHeaders(final ViaHeader... vias) {
        if (vias != null && vias.length > 0) {
            this.viaHeaders = ensureList(this.viaHeaders);
            this.viaHeaders.clear();
            viaHeader = vias[0];
            this.viaHeaders.addAll(Arrays.asList(vias));
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withViaHeaders(final List<ViaHeader> vias) {
        if (vias != null && !vias.isEmpty()) {
            this.viaHeaders = ensureList(this.viaHeaders);
            this.viaHeaders.clear();
            viaHeader = vias.get(0);
            this.viaHeaders.addAll(vias);
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withTopMostViaHeader(final ViaHeader via) {
        if (via != null) {
            this.viaHeaders = ensureList(this.viaHeaders);
            this.viaHeaders.add(0, via);
            viaHeader = via;
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withTopMostViaHeader() {
        this.viaHeaders = ensureList(this.viaHeaders);
        this.viaHeaders.add(0, null);
        return this;
    }

    @Override
    public SipMessage.Builder<T> withPoppedVia() {
        if (this.viaHeaders != null) {
            this.viaHeaders.remove(0);
        }
        return this;
    }

    protected final Function<SipURI, SipURI> getRequestURIFunction() {
        return this.onRequestURIFunction;
    }

    private <T> List<T> ensureList(final List<T> list) {
        if (list != null)  {
            return list;
        }

        // TODO: the initial size of this array could have an impact on performance.
        // TODO: Need to do some performance testing...
        return new ArrayList<T>(4);
    }

    @Override
    public SipMessage.Builder<T> onRequestURI(final Function<SipURI, SipURI> f) {
        if (this.onRequestURIFunction == null) {
            this.onRequestURIFunction = f;
        } else {
            this.onRequestURIFunction = this.onRequestURIFunction.andThen(f);
        }
        return this;
    }

    @Override
    public SipMessage.Builder<T> withBody(final Buffer body) {
        if (body != null) {
            this.body = body.slice();
        }
        return this;
    }

    /**
     * Special size of method that checks the size of the headers
     * we keep track of as a list. The reason we count size() - 1 is
     * because the list already occupy one
     * @param list
     * @return
     */
    private final int sizeOf(final List<?> list) {
        return list == null ? 0 : list.size() - 1;
    }

    /**
     * See {@link SipMessage.Builder#withNoDefaults()}, which describes what defaults will
     * be pushed. They are copied here for reference:
     *
     * <ul>
     *     <li>{@link ToHeader} - the request-uri will be used to construct the to-header</li>
     *     <li>{@link CSeqHeader} - a new CSeq header will be added where the
     *     method is the same as this message and the sequence number is set to 1</li>
     *     <li>{@link CallIdHeader} - a new random call-id will be added</li>
     *     <li>{@link MaxForwardsHeader} - if we are building a request, then a max forwards of 70 will be added</li>
     *     <li>{@link ContentLengthHeader} - Will be added if there is a body
     *     on the message and the length set to the correct length.</li>
     * </ul>
     *
     */
    private void enforceDefaults() {
        if (toHeader == null) {
            withToHeader(generateDefaultToHeader());
        }

        if (isBuildingRequest() && maxForwardsHeader == null) {
            withMaxForwardsHeader(MaxForwardsHeader.create());
        }

        if (callIdHeader == null) {
            withCallIdHeader(CallIdHeader.create());
        }

        if (cSeqHeader == null) {
            withCSeqHeader(generateDefaultCSeqHeader());
        }
    }

    /**
     * Indicates whether or not we are building a request. Must be overridden by
     * the request builder. Used for e.g. {@link SipHeaderBuilderWrapper#enforceDefaults()}
     *
     * @return
     */
    protected boolean isBuildingRequest() {
        return false;
    }

    /**
     * Indicates whether or not we are building a response. Must be overridden by
     * the response builder. Used for e.g. {@link SipHeaderBuilderWrapper#enforceDefaults()}
     *
     * @return
     */
    protected boolean isBuildingResponse() {
        return false;
    }

    protected abstract ToHeader generateDefaultToHeader();

    protected abstract CSeqHeader generateDefaultCSeqHeader();

    @Override
    public T build() {
        int msgSize = 2;

        final int headerCount = this.headers.size() + sizeOf(viaHeaders) + sizeOf(recordRouteHeaders) + sizeOf(routeHeaders);
        final Map<String, List<SipHeader>> finalHeaders = new HashMap<>(headerCount);

        SipHeader contentLengthHeader = null;

        if (useDefaults) {
            enforceDefaults();
        }

        // TODO: redo this, it's ugly. Bloody side effect programming & ugly ugly copy-paste crap
        toHeader = null;
        fromHeader = null;
        cSeqHeader = null;
        callIdHeader = null;
        maxForwardsHeader = null;
        viaHeader = null;
        routeHeader = null;
        recordRouteHeader = null;
        contactHeader = null;
        for (int i = 0; i < this.headers.size(); ++i) {
            final SipHeader header = this.headers.get(i);
            if (header != null) {
                final SipHeader finalHeader = processFinalHeader((short) finalHeaders.size(), header);
                if (finalHeader != null) {
                    if (finalHeader.isContentLengthHeader()) {
                        // not that it actually matters but pretty much
                        // every implementation put the content-length header
                        // last so we'll do that too...
                        contentLengthHeader = finalHeader;
                    } else {
                        msgSize += finalHeader.getBufferSize() + 2;
                        finalHeaders.computeIfAbsent(finalHeader.getName().toString(), k -> new ArrayList<>()).add(finalHeader);
                    }
                }
            }
        }

        if (this.viaHeaders != null) {
            for (int j = 0; j < this.viaHeaders.size(); ++j) {
                final ViaHeader finalVia = processVia(j, this.viaHeaders.get(j));
                msgSize += finalVia.getBufferSize() + 2;
                if (viaHeader == null) {
                    viaHeader = finalVia;
                }

                finalHeaders.computeIfAbsent(finalVia.getName().toString(), k -> new ArrayList<>()).add(finalVia);
            }
        }

        if (this.recordRouteHeaders != null) {
            for (int j = 0; j < this.recordRouteHeaders.size(); ++j) {
                final Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> f =
                                j == 0 ? this.onTopMostRecordRouteBuilder : this.onRecordRouteBuilder;
                final RecordRouteHeader finalRR = invokeAddressBuilderFunction(f, this.recordRouteHeaders.get(j).ensure().toRecordRouteHeader());
                msgSize += finalRR.getBufferSize() + 2;
                if (recordRouteHeader == null) {
                    recordRouteHeader = finalRR;
                }

                finalHeaders.computeIfAbsent(finalRR.getName().toString(), k -> new ArrayList<>()).add(finalRR);
            }
        }

        if (this.routeHeaders != null) {
            for (int j = 0; j < this.routeHeaders.size(); ++j) {
                final Consumer<AddressParametersHeader.Builder<RouteHeader>> f =
                                j == 0 ? this.onTopMostRouteBuilder : this.onRouteBuilder;
                final RouteHeader finalRoute = invokeAddressBuilderFunction(f, this.routeHeaders.get(j).ensure().toRouteHeader());
                msgSize += finalRoute.getBufferSize() + 2;
                if (routeHeader == null) {
                    routeHeader = finalRoute;
                }
                finalHeaders.computeIfAbsent(finalRoute.getName().toString(), k -> new ArrayList<>()).add(finalRoute);
            }
        }

        // TODO: Body - should probably have a onBody as well and we may want
        // to allow a "raw" body as well as an object.
        final Buffer body = this.body;

        // if we are to use defaults then we will adjust the value
        // of the Content-Length header. If not, then the CL will be
        // whatever the user decided it should be.
        if (isBuildingRequest() && useDefaults) {
            contentLengthHeader = ContentLengthHeader.create(body == null ? 0 : body.capacity());
        }

        if (contentLengthHeader != null) {
            msgSize += contentLengthHeader.getBufferSize() + 2;
            finalHeaders.computeIfAbsent(contentLengthHeader.getName().toString(), k -> new ArrayList<>()).add(contentLengthHeader);
        }

        // TODO: not correct but will do for now...
        final SipInitialLine initialLine = buildInitialLine();
        final Buffer initialLineBuffer = initialLine.getBuffer();
        msgSize += initialLineBuffer.getReadableBytes() + 2;

        if (body != null) {
            msgSize += body.getReadableBytes();
        }

        // TODO: instead of copying over the bytes like this create
        // a composite buffer...
        final Buffer msg = Buffers.createBuffer(msgSize);

        initialLineBuffer.getBytes(msg);
        msg.write(SipParser.CR);
        msg.write(SipParser.LF);

        for (final List<SipHeader> headerValues : finalHeaders.values()) {
            for (final SipHeader header : headerValues) {
                header.getBytes(msg);
                msg.write(SipParser.CR);
                msg.write(SipParser.LF);
            }
        }

        msg.write(SipParser.CR);
        msg.write(SipParser.LF);

        if (body != null) {
            body.getBytes(msg);
        }

        return internalBuild(msg,
                             initialLine,
                             finalHeaders,
                             toHeader,
                             fromHeader,
                             cSeqHeader,
                             callIdHeader,
                             maxForwardsHeader,
                             viaHeader,
                             routeHeader,
                             recordRouteHeader,
                             contactHeader,
                             body);
    }

    private ViaHeader processVia(final int index, final SipHeader header) throws SipParseException {
        if (index > 0 && this.onViaBuilder == null) {
            if (header == null) {
                throw new SipParseException("You cannot register an empty Via-header and "
                                            + "then not also register a function for that via. Please refer to javadoc");
            }
            return header.ensure().toViaHeader();
        }

        if (index == 0 && this.onTopMostViaBuilder == null) {
            if (header == null) {
                throw new SipParseException("You cannot register an empty top-most Via-header and "
                                            + "then not also register a function for that top-most via. Please refer to the javadoc");
            }
            return header.ensure().toViaHeader();
        }


        final ViaHeader.Builder builder = header == null ? ViaHeader.builder() : header.ensure().toViaHeader().copy();
        if (index == 0) {
            this.onTopMostViaBuilder.accept(builder);
        } else {
            this.onViaBuilder.accept(index, builder);
        }

        return builder.build();
    }

    protected abstract SipInitialLine buildInitialLine() throws SipParseException;

    protected abstract T internalBuild(final Buffer message,
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
                                       final Buffer body);

    private SipHeader processFinalHeader(final short index, final SipHeader header) {
        final SipHeader finalHeader;

        if (header.isContactHeader()) {
            finalHeader = invokeContactHeaderFunction(header.ensure().toContactHeader());
            contactHeader = finalHeader;
        } else if (header.isCSeqHeader()) {
            finalHeader = header.ensure().toCSeqHeader();
            cSeqHeader = finalHeader;
        } else if (header.isMaxForwardsHeader()) {
            finalHeader = invokeMaxForwardsFunction(header.ensure().toMaxForwardsHeader());
            maxForwardsHeader = finalHeader;
        } else if (header.isFromHeader()) {
            finalHeader = invokeFromHeaderFunction(header.ensure().toFromHeader());
            fromHeader = finalHeader;
        } else if (header.isToHeader()) {
            finalHeader  = invokeToHeaderFunction(header.ensure().toToHeader());
            toHeader = finalHeader;
        } else if (header.isCallIdHeader()) {
            finalHeader = header.ensure().toCallIdHeader();
            callIdHeader = finalHeader;
        } else {
            finalHeader = processGenericHeader(header);
        }

        return finalHeader;
    }

    private <T extends SipHeader> T invokeSipHeaderBuilderFunction(final Consumer<SipHeader.Builder<T>> f,
                                                                   final T header) {
        if (header != null && f != null) {
            final SipHeader.Builder<T> b = header.copy();
            f.accept(b);
            return b.build();
        }
        return header;
    }

    private <T extends AddressParametersHeader> T invokeAddressBuilderFunction(final Consumer<AddressParametersHeader.Builder<T>> f,
                                                                               final T header) {
        if (header != null && f != null) {
            final AddressParametersHeader.Builder<T> b = header.copy();
            f.accept(b);
            return b.build();
        }
        return header;
    }


    private SipHeader processGenericHeader(final SipHeader header) {
        if (this.onHeaderFunction != null) {
            return this.onHeaderFunction.apply(header);
        }

        return header;
    }

    private ToHeader invokeToHeaderFunction(final ToHeader to) {
        if (to != null && onToBuilder != null) {
            final AddressParametersHeader.Builder<ToHeader> b = to.copy();
            onToBuilder.accept(b);
            return b.build();
        }
        return to;
    }

    private ContactHeader invokeContactHeaderFunction(final ContactHeader contact) {
        if (contact != null && onContactBuilder != null) {
            final AddressParametersHeader.Builder<ContactHeader> b = contact.copy();
            onContactBuilder.accept(b);
            return b.build();
        }
        return contact;
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
    public SipMessage.Builder<T> onCommit(final Consumer<SipMessage> f) {
        return this;
    }

    /**
     * Helper function to chain consumers together or return the new one if the current consumer
     * isn't set.
     *
     * @param currentConsumer the current consumer, which may be null
     * @param consumer the new consumer to chain with the current one.
     * @param <T>
     * @return the chained consumer (or the new consumer if there previously wasn't one around)
     */
    private <T> Consumer<T> chainConsumers(final Consumer<T> currentConsumer, final Consumer<T> consumer) {
        if (currentConsumer != null) {
            return currentConsumer.andThen(consumer);
        }

        return consumer;
    }

    private <T, S> BiConsumer<T, S> chainConsumers(final BiConsumer<T, S> currentConsumer, final BiConsumer<T, S> consumer) {
        if (currentConsumer != null) {
            return currentConsumer.andThen(consumer);
        }

        return consumer;
    }

}
