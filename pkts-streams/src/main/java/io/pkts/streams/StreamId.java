/**
 * 
 */
package io.pkts.streams;

/**
 * @author jonas@jonasborjesson.com
 */
public interface StreamId {

    /**
     * Convert this {@link StreamId} to a string. It MUST be possible to
     * recreate this {@link StreamId} from this string.
     * 
     * @return
     */
    String asString();

}
