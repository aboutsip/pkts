/**
 * 
 */
package io.pkts.filters;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.Frame;
import io.pkts.frame.SipFrame;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipParseException;
import io.pkts.protocol.Protocol;

import java.io.IOException;


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
