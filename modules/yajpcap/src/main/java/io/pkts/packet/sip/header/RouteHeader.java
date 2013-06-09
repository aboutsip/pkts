/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

/**
 * Source: RFC 3261 section 20.30
 * 
 * <p>
 * The Route header field is used to force routing for a request through the
 * listed set of proxies. Examples of the use of the Route header field are in
 * Section 16.12.1.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 *    Route: &lt;sip:bigbox3.site3.atlanta.com;lr&gt;,
 *           &lt;sip:server10.biloxi.com;lr&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RouteHeader extends HeaderAddress, Parameters, SipHeader {

    Buffer NAME = Buffers.wrap("Route");

    RouteHeader clone();

}
