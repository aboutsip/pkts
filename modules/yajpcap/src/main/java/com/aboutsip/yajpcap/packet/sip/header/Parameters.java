/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Parameters {

    /**
     * Get the value of the named parameter. If the named parameter is a
     * so-called flag parameter, then the value returned will be an empty
     * {@link Buffer}, which can be checked with {@link Buffer#isEmpty()} or
     * {@link Buffer#capacity()}, which will return zero. As with any empty
     * {@link Buffer}, if you do {@link Buffer#toString()} you will be getting
     * an empty {@link String} back, which would be yet another way to check for
     * a flag parameter.
     * 
     * @param name
     *            the name of the parameter we are looking for.
     * @return the value of the named parameter or null if there is no such
     *         parameter. If the named parameter is a flag parameter, then an
     *         empty buffer will be returned.
     * @throws SipParseException
     *             in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null.
     */
    Buffer getParameter(Buffer name) throws SipParseException, IllegalArgumentException;

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null.
     */
    Buffer getParameter(String name) throws SipParseException, IllegalArgumentException;

}
