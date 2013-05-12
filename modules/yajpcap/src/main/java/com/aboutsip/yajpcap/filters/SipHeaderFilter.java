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
import com.aboutsip.yajpcap.packet.sip.header.SipHeader;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderFilter extends SipFilter {
    private final Buffer name;
    private final Buffer value;

    public SipHeaderFilter(final String name, final String value) {
        this.name = Buffers.wrap(name);
        this.value = Buffers.wrap(value);
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
                final SipHeader header = msg.getHeader(this.name);
                if (header == null) {
                    return false;
                }

                return header.getValue().equals(this.value);
            }
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        } catch (final PacketParseException e) {
            throw new FilterException("Unable to process the frame due to parse issue of the SIP Message", e);
        }
        return false;
    }

}
