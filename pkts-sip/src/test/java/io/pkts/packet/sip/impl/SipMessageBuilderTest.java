package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipMessage.Builder;
import io.pkts.packet.sip.header.MaxForwardsHeader;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageBuilderTest extends PktsTestBase {

    /**
     *
     * @throws Exception
     */
    @Test
    public void testBasicCopy() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInvite);
        final Builder<SipMessage> builder = msg.copy();
        System.out.println(msg);

        // builder.onMaxForwardsHeader(max -> max.copy().decrement());
        builder.onMaxForwardsHeader(this::decrement);
        final SipMessage copy = builder.build();

        assertThat(copy.getMaxForwards().getMaxForwards(), is(msg.getMaxForwards().getMaxForwards() - 1));
        System.err.println(copy);
    }

    MaxForwardsHeader.Builder decrement(final MaxForwardsHeader max) {
        System.err.println(max);
        System.err.println(max.copy());
        return max.copy().decrement();
    }



    /*
    public void testBuilderPattern() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInvite);
        // msg.copy().headerStream().filter().onHeader(b -> b.)
        // msg.createResponse(200).onFrom();
        // msg.copy().filter(predicate);

        // SipRequest.invite("sip:alice@example.com").withNoDefaults();

        // copy everything but change the user portion of the
        // from address to "nisse"
        msg.copy().onFromHeader(from -> Optional.of(from.copy().withUser("nisse"))).build();

        // copy everything except X-headers.
        msg.copy().filter(h -> !h.getName().toString().startsWith("X-")).build();

        // include all headers and add the user=phone as a parameter to the request uri
        msg.copy().onRequestURI(uri -> uri.copy().withParameter("user", "phone")).build();

        // use method pointers for filtering and manipulating the headers
        msg.copy().filter(this::filter).onHeader(this::manipulateHeader).build();

        // tomorrow, what about indicating we want a default new via header?;
        // how can we check so that it wasnt already added by someone else further up in the stack?;
        // Perhaps pushVia() is enough and that will create a new ViaHeader.Builder() if one doesnt;
        // already exist and if multiple calls to pushVia is made those are then no-ops...

        // SipRequest.invite("sip:alice@example.com").withDefaultTo().withFrom(from).withContact(cBuilder).withDefaultCSeq().withDefaultCallId().withHeader().withHeader().withBody().build();
        // filter(name -> !name.startsWith("X-")).onContact(cBuilder).onHeader(headerBuilder);
    }
    */

    /**
     * Only keep headers starting with 'A'
     * @param header
     * @return
     */
    /*
    private boolean filter(final SipHeader header) {
        try {
            return header.getName().getByte(0) == 'A';
        } catch (final IOException e) {
            // TODO: once we have fixed the buffer stuff there will no
            // longer be an IO exception here!
            return false;
        }
    }
    */

    /**
     * Example of a method that manipulates headers by creating a copy of it and return that
     * which the sip message builder then will eventually build. Also note how this method
     * uses the ensure to make sure that the header is fully parsed etc.
     *
     * Note, parsing every header is expensive.
     *
     * @param header
     * @return
     */
    /*
    public Optional<SipHeader.Builder> manipulateHeader(final SipHeader header) {

        // TODO: perhaps rename the ensure method to "parseIt" or something like that?
        final SipHeader parsedForSure = header.ensure();
        if (parsedForSure.isAddressParametersHeader()) {
            final AddressParametersHeader h = parsedForSure.toAddressParametersHeader();
            return Optional.of(h.copy().withHost("pkts.io"));
        }

        return Optional.empty();
    }
    */
}