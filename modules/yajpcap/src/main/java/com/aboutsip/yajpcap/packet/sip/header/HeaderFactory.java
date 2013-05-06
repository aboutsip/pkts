/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface HeaderFactory {

    ViaHeader createViaHeader(Buffer host, int port, Buffer transport, Buffer branch);

}
