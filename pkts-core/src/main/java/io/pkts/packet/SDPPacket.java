/**
 * 
 */
package io.pkts.packet;

import io.pkts.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SDPPacket extends Packet {

    Buffer toBuffer();

}
