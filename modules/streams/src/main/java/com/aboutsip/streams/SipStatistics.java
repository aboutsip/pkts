/**
 * 
 */
package com.aboutsip.streams;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipStatistics {

    /**
     * The total number of SIP messages processed by the {@link StreamHandler}.
     * This includes re-transmits also.
     * 
     * @return
     */
    long totalSipMessages();

    /**
     * The total number of SIP INVITE requests processed by the
     * {@link StreamHandler}, which also includes the re-transmitted ones.
     * 
     * @return
     */
    long totalInviteRequests();

    long totalAckRequests();

    long totalByeRequests();

    long totalOptionsRequests();

    long totalInfoRequests();

    long totalCancelRequests();

    /**
     * Dump all stats to std out. Typically you don't want to use this. Mainly
     * for debugging purposes.
     * 
     * @return
     */
    String dumpInfo();
}
