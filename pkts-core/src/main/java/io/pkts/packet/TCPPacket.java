/**
 * 
 */
package io.pkts.packet;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface TCPPacket extends TransportPacket {

    int getChecksum();

    int getUrgentPointer();

    int getWindowSize();

    short getReserved();

    boolean isNS();

    boolean isFIN();

    boolean isSYN();

    boolean isRST();

    /**
     * Check whether the psh (push) flag is turned on
     * 
     * @return
     */
    boolean isPSH();

    boolean isACK();

    boolean isURG();

    boolean isECE();

    boolean isCWR();

    long getSequenceNumber();

    long getAcknowledgementNumber();

    @Override
    IPPacket getParentPacket();
}
