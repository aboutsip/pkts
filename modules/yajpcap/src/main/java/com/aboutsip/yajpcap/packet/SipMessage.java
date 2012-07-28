package com.aboutsip.yajpcap.packet;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.impl.sip.SipParseException;

/**
 * Packet representing a SIP message.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipMessage extends Packet {

    /**
     * Check whether this sip message is a response or not
     * 
     * @return
     */
    boolean isResponse();

    /**
     * Check whether this sip message is a request or not
     * 
     * @return
     */
    boolean isRequest();

    /**
     * Get the method of this sip message
     * 
     * @return
     */
    Buffer getMethod() throws SipParseException;

    /**
     * Get the header as a buffer
     * 
     * @param headerName the name of the header we wish to fetch
     * @return the header as a buffer or null if not found
     */
    SipHeader getHeader(Buffer headerName) throws SipParseException;

    /**
     * Convenience method for fetching the from-header
     * 
     * @return the from header as a buffer
     * @throws SipParseException TODO
     */
    SipHeader getFromHeader() throws SipParseException;

    /**
     * Convenience method for fetching the to-header
     * 
     * @return the to header as a buffer
     */
    SipHeader getToHeader() throws SipParseException;

    /**
     * Convenience method for fetching the call-id-header
     * 
     * @return the call-id header as a buffer
     */
    SipHeader getCallIDHeader() throws SipParseException;


    /**
     * {@inheritDoc}
     * 
     * <p>
     * In the case of a SIP message the following checks are conducted: The
     * following checks are available:
     * 
     * <ul>
     * <li>ruri sip version - checks if the SIP version in the request URI is
     * supported, currently only 2.0.</li>
     * <li>ruri scheme - checks if the URI scheme of the request URI is
     * supported (sip[s]|tel[s]) by SIP-router.</li>
     * <li>required headers - checks if the minimum set of required headers to,
     * from, cseq, callid and via is present in the request.</li>
     * <li>via sip version - not working because parser fails already when
     * another version then 2.0 is present.</li>
     * <li>via protocol - not working because parser fails already if an
     * unsupported transport is present.</li>
     * <li>cseq method - checks if the method from the cseq header is equal to
     * the request method.</li>
     * <li>cseq value - checks if the number in the cseq header is a valid
     * unsigned integer.</li>
     * <li>content length - checks if the size of the body matches with the
     * value from the content length header.</li>
     * <li>expires value - checks if the value of the expires header is a valid
     * unsigned integer.</li>
     * <li>proxy require - checks if all items of the proxy require header are
     * present in the list of the extensions from the module parameter
     * proxy_require.</li>
     * 
     * <li>parse uri's - checks if the specified URIs are present and parseable
     * by the SIP-router parsers</li>
     * <li>digest credentials - Check all instances of digest credentials in a
     * message. The test checks whether there are all required digest parameters
     * and have meaningful values.</li>
     * </ul>
     * </p>
     * 
     * <p>
     * This list is taken from <a href=
     * "http://kamailio.org/docs/modules/stable/modules/sanity.html#sanity_check"
     * >Kamailio.org</a>
     * </p>
     * 
     */
    @Override
    void verify();

}
