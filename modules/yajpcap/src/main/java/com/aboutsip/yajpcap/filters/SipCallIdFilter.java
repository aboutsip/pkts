/**
 * 
 */
package com.aboutsip.yajpcap.filters;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipCallIdFilter extends SipFilter {

    private final Buffer callId;

    public SipCallIdFilter(final String callId) {
        this.callId = Buffers.wrap(callId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Frame frame) throws FilterException {
        try {
            if (super.accept(frame)) {
                final SipFrame sipFrame = (SipFrame) frame.getFrame(Protocol.SIP);
                final SipMessage msg = sipFrame.parse();
                return msg.getCallIDHeader().getValue().equals(this.callId);
            }
        } catch (final SipParseException e) {
            throw new FilterException("Unable to process the frame due to SipParseException", e);
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        } catch (final PacketParseException e) {
            throw new FilterException("Unable to process the frame due to parse issue of the SIP Message", e);
        }
        return false;
    }

    public String getCallId() {
        return this.callId.toString();
    }

}
