/**
 * 
 */
package com.aboutsip.yajpcap.packet;


/**
 * Represents a captured packet.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Packet {
    /**
     * Calling this method will force the packet to completely parse its data
     * and check so that all the information conforms to whatever rules this
     * packet needs to follow. E.g., if this happens to be a SIP packet, then it
     * will check if it has the mandatory headers etc.
     * 
     * Some simpler packets, such as the {@link IPPacket}, hardly does anything
     * in this method but more complex protocols such as SIP (once again), HTTP
     * etc can spend quite some time verifying everything, which is why you
     * don't want to do it unless you really have to.
     * 
     * In general, yajpcap has the philosophy of
     * "assume that everything is ok until things blow up and then deal with it"
     */
    void verify();

}
