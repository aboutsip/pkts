package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;

import java.util.List;
import java.util.Map;





/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseBuilder extends SipMessageBuilder<SipResponse> implements SipResponse.Builder {

    /**
     * Map of all known response codes to their default reason phrases.
     */
    private static final Buffer[] DEFAULT_RESPONSE_REASON = new Buffer[700];

    /**
     * If the user creates a response that we do not recognize.
     */
    private static final Buffer UNKOWN_REASON = Buffers.wrap("Unknown");

    static {
        // 1xx—Provisional Responses
        DEFAULT_RESPONSE_REASON[100] = Buffers.wrap("Trying");
        DEFAULT_RESPONSE_REASON[180] = Buffers.wrap("Ringing");
        DEFAULT_RESPONSE_REASON[181] = Buffers.wrap("Call is Being Forwarded");
        DEFAULT_RESPONSE_REASON[182] = Buffers.wrap("Queued");
        DEFAULT_RESPONSE_REASON[183] = Buffers.wrap("Session Progress");
        DEFAULT_RESPONSE_REASON[199] = Buffers.wrap("Early Dialog Terminated");

        // 2xx—Successful Responses
        DEFAULT_RESPONSE_REASON[200] = Buffers.wrap("OK");
        DEFAULT_RESPONSE_REASON[202] = Buffers.wrap("Accepted");
        DEFAULT_RESPONSE_REASON[204] = Buffers.wrap("No Notification");

        // 3xx—Redirection Responses
        DEFAULT_RESPONSE_REASON[300] = Buffers.wrap("Multiple Choices");
        DEFAULT_RESPONSE_REASON[301] = Buffers.wrap("Moved Permanently");
        DEFAULT_RESPONSE_REASON[302] = Buffers.wrap("Moved Temporarily");
        DEFAULT_RESPONSE_REASON[305] = Buffers.wrap("Use Proxy");
        DEFAULT_RESPONSE_REASON[380] = Buffers.wrap("Alternative Service");

        // 4xx—Client Failure Responses
        DEFAULT_RESPONSE_REASON[400] = Buffers.wrap("Bad Request");
        DEFAULT_RESPONSE_REASON[401] = Buffers.wrap("Unauthorized");
        DEFAULT_RESPONSE_REASON[402] = Buffers.wrap("Payment Required");
        DEFAULT_RESPONSE_REASON[403] = Buffers.wrap("Forbidden");
        DEFAULT_RESPONSE_REASON[404] = Buffers.wrap("Not Found");
        DEFAULT_RESPONSE_REASON[405] = Buffers.wrap("Method Not Allowed");
        DEFAULT_RESPONSE_REASON[406] = Buffers.wrap("Not Acceptable");
        DEFAULT_RESPONSE_REASON[407] = Buffers.wrap("Proxy Authentication Required");
        DEFAULT_RESPONSE_REASON[408] = Buffers.wrap("Request Timeout");
        DEFAULT_RESPONSE_REASON[409] = Buffers.wrap("Conflict");
        DEFAULT_RESPONSE_REASON[410] = Buffers.wrap("Gone");
        DEFAULT_RESPONSE_REASON[411] = Buffers.wrap("Length Required");
        DEFAULT_RESPONSE_REASON[412] = Buffers.wrap("Conditional Request Failed");
        DEFAULT_RESPONSE_REASON[413] = Buffers.wrap("Request Entity Too Large");
        DEFAULT_RESPONSE_REASON[414] = Buffers.wrap("Request-URI Too Long");
        DEFAULT_RESPONSE_REASON[415] = Buffers.wrap("Unsupported Media Type");
        DEFAULT_RESPONSE_REASON[416] = Buffers.wrap("Unsupported URI Scheme");
        DEFAULT_RESPONSE_REASON[417] = Buffers.wrap("Unknown Resource-Priority");
        DEFAULT_RESPONSE_REASON[420] = Buffers.wrap("Bad Extension");
        DEFAULT_RESPONSE_REASON[421] = Buffers.wrap("Extension Required");
        DEFAULT_RESPONSE_REASON[422] = Buffers.wrap("Session Interval Too Small");
        DEFAULT_RESPONSE_REASON[423] = Buffers.wrap("Interval Too Brief");
        DEFAULT_RESPONSE_REASON[424] = Buffers.wrap("Bad Location Information");
        DEFAULT_RESPONSE_REASON[428] = Buffers.wrap("Use Identity Header");
        DEFAULT_RESPONSE_REASON[429] = Buffers.wrap("Provide Referrer Identity");
        DEFAULT_RESPONSE_REASON[430] = Buffers.wrap("Flow Failed");
        DEFAULT_RESPONSE_REASON[433] = Buffers.wrap("Anonymity Disallowed");
        DEFAULT_RESPONSE_REASON[436] = Buffers.wrap("Bad Identity-Info");
        DEFAULT_RESPONSE_REASON[437] = Buffers.wrap("Unsupported Certificate");
        DEFAULT_RESPONSE_REASON[438] = Buffers.wrap("Invalid Identity Header");
        DEFAULT_RESPONSE_REASON[439] = Buffers.wrap("First Hop Lacks Outbound Support");
        DEFAULT_RESPONSE_REASON[470] = Buffers.wrap("Consent Needed");
        DEFAULT_RESPONSE_REASON[480] = Buffers.wrap("Temporarily Unavailable");
        DEFAULT_RESPONSE_REASON[481] = Buffers.wrap("Call/Transaction Does Not Exist");
        DEFAULT_RESPONSE_REASON[482] = Buffers.wrap("Loop Detected.");
        DEFAULT_RESPONSE_REASON[483] = Buffers.wrap("Too Many Hops");
        DEFAULT_RESPONSE_REASON[484] = Buffers.wrap("Address Incomplete");
        DEFAULT_RESPONSE_REASON[485] = Buffers.wrap("Ambiguous");
        DEFAULT_RESPONSE_REASON[486] = Buffers.wrap("Busy Here");
        DEFAULT_RESPONSE_REASON[487] = Buffers.wrap("Request Terminated");
        DEFAULT_RESPONSE_REASON[488] = Buffers.wrap("Not Acceptable Here");
        DEFAULT_RESPONSE_REASON[489] = Buffers.wrap("Bad Event");
        DEFAULT_RESPONSE_REASON[491] = Buffers.wrap("Request Pending");
        DEFAULT_RESPONSE_REASON[493] = Buffers.wrap("Undecipherable");
        DEFAULT_RESPONSE_REASON[494] = Buffers.wrap("Security Agreement Required");

        // 5xx—Server Failure Responses
        DEFAULT_RESPONSE_REASON[500] = Buffers.wrap("Server Internal Error");
        DEFAULT_RESPONSE_REASON[501] = Buffers.wrap("Not Implemented");
        DEFAULT_RESPONSE_REASON[502] = Buffers.wrap("Bad Gateway");
        DEFAULT_RESPONSE_REASON[503] = Buffers.wrap("Service Unavailable");
        DEFAULT_RESPONSE_REASON[504] = Buffers.wrap("Server Time-out");
        DEFAULT_RESPONSE_REASON[505] = Buffers.wrap("Version Not Supported");
        DEFAULT_RESPONSE_REASON[513] = Buffers.wrap("Message Too Large");
        DEFAULT_RESPONSE_REASON[580] = Buffers.wrap("Precondition Failure");

        // 6xx—Global Failure Responses
        DEFAULT_RESPONSE_REASON[600] = Buffers.wrap("Busy Everywhere");
        DEFAULT_RESPONSE_REASON[603] = Buffers.wrap("Decline");
        DEFAULT_RESPONSE_REASON[604] = Buffers.wrap("Does Not Exist Anywhere");
        DEFAULT_RESPONSE_REASON[606] = Buffers.wrap("Not Acceptable");
    }

    private final int statusCode;

    private Buffer reason;

    public SipResponseBuilder(final int statusCode) {
        // TODO: do some research of what the typical SIP Response header size is.
        super(10);
        this.statusCode = statusCode;
    }

    @Override
    final protected boolean isBuildingResponse() {
        return true;
    }

    @Override
    protected ToHeader generateDefaultToHeader() {
        throw new SipParseException("Not implemented yet and also not sure what a default To-header would be for a response");
    }

    /**
     * For a response the only way we know which method this response is for
     * is from the CSeq-header so if that one doesn't exist then there
     * is simply nothing we can do.
     *
     * @return
     */
    @Override
    protected CSeqHeader generateDefaultCSeqHeader() {
        throw new SipParseException("Unable to generate a default CSeq-header for the response. You must specify it");
    }

    @Override
    protected SipInitialLine buildInitialLine() throws SipParseException {
        return new SipResponseLine(statusCode, reason != null ? reason : getDefaultResponseReason(statusCode));
    }

    @Override
    protected SipResponse internalBuild(final Buffer msg, final SipInitialLine initialLine,
                                        final Map<String, List<SipHeader>> headers, final SipHeader toHeader,
                                        final SipHeader fromHeader, final SipHeader cSeqHeader,
                                        final SipHeader callIdHeader, final SipHeader maxForwardsHeader,
                                        final SipHeader viaHeader, final SipHeader routeHeader,
                                        final SipHeader recordRouteHeader, final SipHeader contactHeader,
                                        final Buffer body) {

        return new ImmutableSipResponse(msg,
                                        initialLine.toResponseLine(),
                                        headers,
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

    @Override
    public SipResponse.Builder withReasonPhrase(final String reason) {
        if (reason != null && !reason.isEmpty()) {
            this.reason = Buffers.wrap(reason);
        }
        return this;
    }

    @Override
    public SipResponse.Builder withReasonPhrase(final Buffer reason) {
        if (reason != null && !reason.isEmpty()) {
            // TODO: once buffer is immutable we don't have to do the slice.
            this.reason = reason.slice();
        }
        return this;
    }

    private static Buffer getDefaultResponseReason(final int statusCode) {
        final Buffer reason = DEFAULT_RESPONSE_REASON[statusCode];
        if (reason != null) {
            return reason.slice(); // really need to create immutable buffers
        }
        return UNKOWN_REASON.slice();
    }


}
