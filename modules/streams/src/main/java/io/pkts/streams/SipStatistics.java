/**
 * 
 */
package io.pkts.streams;

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
     * This will return an array with 600 elements, each index representing a
     * particular response (but with an offset of negative 100). E.g., if you
     * want to check how many 180 Ringing responses was captured, just check
     * what the count at index 180 - 100 = 80 is like so:
     * 
     * <code>
     * System.out.println("Total 180 responses: " + totalResponses()[180 - 100]);
     * </code>
     * 
     * Here is another example that prints out all responses received and their
     * count:
     * 
     * <code>
     * final int[] responses = stats.totalResponses();
     * System.out.println("All responses");
     * for (final int i = 0; i < responses.length; ++i) {
     *     if (responses[i] > 0) {
     *         System.out.println((i + 100) + ": " + responses[i]);
     *     }
     * }
     * </code>
     * 
     * Notice how we add 100 to the index as it is printed.
     * 
     * @return
     */
    int[] totalResponses();

    /**
     * Dump all stats to std out. Typically you don't want to use this. Mainly
     * for debugging purposes.
     * 
     * @return
     */
    String dumpInfo();
}
