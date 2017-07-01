/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.impl.SipResponseBuilder;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipResponse extends SipMessage {

    /**
     * Get the status code of this SIP response
     *
     * @return
     */
    int getStatus();

    /**
     * Get the reason phrase of this {@link SipResponse}
     *
     * @return
     */
    Buffer getReasonPhrase();

    @Override
    default boolean isResponse() {
        return true;
    }

    /**
     * Convenience method for checking whether this response is >= 400.
     *
     * @return
     */
    default boolean isError() {
        return getStatus() / 100 >= 4;
    }

    /**
     * Convenience method for checking whether this is a 1xx response or not.
     *
     * @return
     */
    default boolean isProvisional() {
        return getStatus() / 100 == 1;
    }

    /**
     * Convenience method for checking whether this response is a final response, i.e. any response
     * >= 200.
     *
     * @return
     */
    default boolean isFinal() {
        return getStatus() >= 200;
    }

    /**
     * Convenience method for checking whether this is a 2xx response or not.
     *
     * @return
     */
    default boolean isSuccess() {
        return getStatus() / 100 == 2;
    }

    /**
     * Convenience method for checking whether this is a 3xx response or not.
     *
     * @return
     */
    default boolean isRedirect() {
        return getStatus() / 100 == 3;
    }

    /**
     * Convenience method for checking whether this is a 4xx response or not.
     *
     * @return
     */
    default boolean isClientError() {
        return getStatus() / 100 == 4;
    }

    /**
     * Convenience method for checking whether this is a 5xx response or not.
     *
     * @return
     */
    default boolean isServerError() {
        return getStatus() / 100 == 5;
    }

    /**
     * Convenience method for checking whether this is a 6xx response or not.
     *
     * @return
     */
    default boolean isGlobalError() {
        return getStatus() / 100 == 6;
    }

    /**
     * Convenience method for checking whether this is a 100 Trying response or
     * not.
     *
     * @return
     */
    default boolean is100Trying() {
        return getStatus() == 100;
    }

    /**
     * Convenience method for checking whether this is a 180 Ringing response or
     * or a 183 Early Media response.
     *
     * @return true if this response is a 180 or a 183 response, false otherwise
     */
    default boolean isRinging() {
        return getStatus() == 180 || getStatus() == 183;
    }

    static Builder withStatusCode(final int statusCode) {
        return new SipResponseBuilder(statusCode);
    }

    /**
     * Convenience method for checking whether this is a 480 Timeout response or
     * not.
     *
     * @return
     */
    default boolean isTimeout() {
        return getStatus() == 480;
    }

    default SipResponse toResponse() throws ClassCastException {
        return this;
    }

    @Override
    Builder copy();

    @Override
    SipResponse clone();

    interface Builder extends SipMessage.Builder<SipResponse> {

        @Override
        default SipMessage.Builder<SipResponse> toSipResponseBuilder() {
            return this;
        }

        @Override
        default boolean isSipResponseBuilder() {
            return true;
        }

        Builder withReasonPhrase(String reason);

        Builder withReasonPhrase(Buffer reason);

        @Override
        SipResponse build();

    }

}
