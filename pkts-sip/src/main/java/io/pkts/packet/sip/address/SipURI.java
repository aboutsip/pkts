/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipURI extends URI {

    // Once we swap over to Java 8 we can do this instead,
    // which looks so much better...
    // public static SipURIBuilder with() {
    // return SipURIBuilder.with();
    // }

    /**
     * Get the user portion of this URI.
     * 
     * @return the user portion of this URI or an empty buffer if there is no
     *         user portion
     */
    Buffer getUser();

    /**
     * Get the host portion of this URI.
     * 
     * @return
     */
    Buffer getHost();

    /**
     * Get the port. If the port isn't set then -1 (negative one) will be
     * returned.
     * 
     * @return
     */
    int getPort();

    /**
     * Set the port.
     * 
     * If you specify -1 (negative one), then no port will be included in the final SIP URI.
     * However, if you explicitly set the port to something, then it will be included in the final
     * result.
     * 
     * @param port
     * @return
     */
    void setPort(int port);

    /**
     * Check whether this is a sips URI.
     * 
     * @return true if this indeed is a sips URI, false otherwise.
     */
    boolean isSecure();

    /**
     * Get the value of the named parameter. If the named parameter is a so-called flag parameter,
     * then the value returned will be an empty {@link Buffer}, which can be checked with
     * {@link Buffer#isEmpty()} or {@link Buffer#capacity()}, which will return zero. As with any
     * empty {@link Buffer}, if you do {@link Buffer#toString()} you will be getting an empty
     * {@link String} back, which would be yet another way to check for a flag parameter.
     * 
     * @param name the name of the parameter we are looking for.
     * @return the value of the named parameter or null if there is no such parameter. If the named
     *         parameter is a flag parameter, then an empty buffer will be returned.
     * @throws SipParseException in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException in case the name is null.
     */
    Buffer getParameter(Buffer name) throws SipParseException, IllegalArgumentException;

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     * @throws SipParseException in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException in case the name is null.
     */
    Buffer getParameter(String name) throws SipParseException, IllegalArgumentException;

    /**
     * Sets the value of the specified parameter. If there already is a parameter with the same name
     * its value will be overridden.
     * 
     * A value of null or a zero length buffer means that this parameter is a flag parameter
     * 
     * @param name the name of the parameter
     * @param value the value of the parameter or null if you just want to set a flag parameter
     * @return the previous value or null if there were none associated with this parameter name
     * @throws SipParseException in case anything goes wrong when setting the parameter.
     * @throws IllegalArgumentException in case the name is null or empty.
     */
    Buffer setParameter(Buffer name, Buffer value) throws SipParseException, IllegalArgumentException;


    /**
     * Get the entire content of the {@link SipURI} as a {@link Buffer}.
     * 
     * @return
     */
    Buffer toBuffer();

}
