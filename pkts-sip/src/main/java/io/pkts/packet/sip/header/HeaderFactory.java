/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.address.Address;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface HeaderFactory {


    FromHeader createFromHeader(Address from, Buffer tag);


    ToHeader createToHeader(Address to, Buffer tag);

}
