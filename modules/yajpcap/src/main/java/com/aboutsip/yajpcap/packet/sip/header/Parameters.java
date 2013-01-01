/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Parameters {

    /**
     * Get the value of the named parameter.
     * 
     * @param name
     *            the name of the parameter we are looking for.
     * @return the value of the named parameter or null if there is no such
     *         parameter
     */
    Buffer getParameter(Buffer name);

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     */
    Buffer getParameter(String name);

}
