/**
 * 
 */
package io.pkts.packet;

import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipMessageFactory {

    /**
     * Create a new response based on the supplied {@link SipRequest}. Only the
     * mandatory headers from the {@link SipRequest} are copied. Those mandatory
     * headers are:
     * <ul>
     * <li>{@link ToHeader}</li>
     * <li>{@link FromHeader}</li>
     * <li>{@link CallIdHeader}.</li>
     * <li>{@link CSeqHeader}</li>
     * <li>{@link MaxForwardsHeader}</li>
     * </ul>
     * 
     * @param statusCode
     * @param request
     * @return
     * @throws SipParseException
     *             in case anything goes wrong when parsing out headers from the
     *             {@link SipRequest}
     */
    SipResponse createResponse(int statusCode, SipRequest request) throws SipParseException;

    /**
     * Creates a new {@link SipRequest} using the original request as a
     * template.
     * 
     * @param originalRequest
     * @return
     * @throws SipParseException
     */
    SipRequest createRequest(SipRequest originalRequest) throws SipParseException;

}
