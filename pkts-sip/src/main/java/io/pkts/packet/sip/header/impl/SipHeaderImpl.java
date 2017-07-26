/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.impl.SipParser;

import java.util.function.Function;


/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderImpl implements SipHeader {

    private final Buffer name;

    private final Buffer value;

    /**
     * 
     */
    public SipHeaderImpl(final Buffer name, final Buffer value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public SipHeader.Builder<? extends SipHeader> copy() {
        return new SipHeaderBuilder(name, value);
    }

    /**
     * Subclasses may override this one and are in fact encourage to do so
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        return this.value.slice();
    }

    @Override
    public String toString() {
        return getName().toString() + ": " + getValue();
    }

    @Override
    public void verify() throws SipParseException {
        // by default, everything is assumed to be correct.
        // Subclasses should override this method and
        // check that everything is ok...
    }

    /**
     * If this method actually gets called it means that we are the {@link SipHeaderImpl} itself
     * and that we need to frame it further. Subclasses MUST override this method and simply return
     * 'this'.
     */
    @Override
    public SipHeader ensure() {
        final Function<SipHeader, ? extends SipHeader> framer = SipParser.getFramer(this.name);
        if (framer != null) {
            return framer.apply(this);
        }
        return this;
    }

    @Override
    public void getBytes(final Buffer dst) {
        this.name.getBytes(0, dst);
        dst.write(SipParser.COLON);
        dst.write(SipParser.SP);
        transferValue(dst);
    }

    /**
     * Transfer the bytes of the value into the destination. Sub-classes should
     * override this method.
     * 
     * @param dst
     */
    protected void transferValue(final Buffer dst) {
        final Buffer value = getValue();
        value.getBytes(0, dst);
    }

    @Override
    public SipHeader clone() {
        // TODO: this will be easier once everything is immutable since you just have to clone the
        // value buffer and that is it. No need to transfer the value etc.
        // final Buffer buffer = Buffers.createBuffer(1024);
        // transferValue(buffer);
        return new SipHeaderImpl(this.name.clone(), value.clone());
    }

}
