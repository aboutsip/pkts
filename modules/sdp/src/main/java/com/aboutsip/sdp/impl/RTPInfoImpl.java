/**
 * 
 */
package com.aboutsip.sdp.impl;

import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.SdpParseException;

import com.aboutsip.sdp.RTPInfo;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RTPInfoImpl implements RTPInfo {

    /**
     * The c-field (connection) of the SDP. If null, then the connection
     * information should be retrieved from the {@link MediaDescription}.
     */
    private final Connection connection;

    private final MediaDescription mediaDescription;

    public RTPInfoImpl(final Connection connection, final MediaDescription mediaDescription) {
        this.connection = mediaDescription.getConnection() != null ? mediaDescription.getConnection() : connection;
        this.mediaDescription = mediaDescription;
    }

    @Override
    public String toString() {
        return this.connection + this.mediaDescription.toString();
    }

    @Override
    public String getAddress() {
        try {
            if (this.connection != null) {
                return this.connection.getAddress();
            }

            return this.mediaDescription.getConnection().getAddress();
        } catch (final SdpParseException e) {
            throw new RuntimeException("TODO: real exception pls", e);
        }
    }

    @Override
    public int getMediaPort() {
        try {
            return this.mediaDescription.getMedia().getMediaPort();
        } catch (final SdpParseException e) {
            throw new RuntimeException("TODO: real exception pls", e);
        }
    }

}
