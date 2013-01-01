/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.header.Parameters;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ParametersImpl extends SipHeaderImpl implements Parameters {

    private Buffer params = null;

    /**
     * @throws SipParseException
     * 
     */
    public ParametersImpl(final Buffer name, final Buffer value) {
        super(name, value);
        try {
            final int index = SipParser.indexOf(value, SipParser.SEMI);
            if (index > -1) {
                this.params = value.slice(index, value.capacity());
                SipParser.consumeSEMI(value);
            } else {
                this.params = null;
            }
        } catch (final IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SipParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public Buffer getParameter(final Buffer name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Buffer getParameter(final String name) {
        return getParameter(Buffers.wrap(name));
    }

}
