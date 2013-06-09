/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface HeaderFactory {

    ViaHeader createViaHeader(Buffer host, int port, Buffer transport, Buffer branch);

}
