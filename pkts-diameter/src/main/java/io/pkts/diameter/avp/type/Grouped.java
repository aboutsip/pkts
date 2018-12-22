package io.pkts.diameter.avp.type;

import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.avp.RawAvp;
import io.pkts.diameter.impl.DiameterParser;

import java.util.ArrayList;
import java.util.List;

public interface Grouped extends DiameterType {

    static Grouped parse(final RawAvp groupedAvp) {
        // for grouped, we expect there to be x number of AVPs present
        // in the data and while we have data left to read, we'll continue
        // to find new ones. Note, we will not go deep, only one "layer"
        // at a time and then you will have to call the parse method again
        // if you wish to keep diving deeper and deeper into the grouped ones.
        // The reason is that we don't want to spend time on parsing if the
        // user isn't going to use it.

        // perhaps early optimization warning here but trying to limit the
        // array of AVPs to the maximum expected AVPs. By default, the java
        // array list capacity is 10, which is rare to have so let's caluclate
        // the expected maximum AVPs we can get (smallest AVP header size of 8 and 4 bytes of data)
        // if we guesstimate too low, the list will grow anyway so it's fine.
        final int maxNoOfAvps = groupedAvp.getHeader().getLength() / (8 + 4);
        final List<RawAvp> avps = new ArrayList<>(maxNoOfAvps);
        final ReadOnlyBuffer data = (ReadOnlyBuffer) groupedAvp.getData();
        while (data.hasReadableBytes()) {
            avps.add(DiameterParser.frameRawAvp(data));
        }

        return new DefaultGrouped(avps);
    }

    class DefaultGrouped implements Grouped {
        final List<RawAvp> avps;

        private DefaultGrouped(final List<RawAvp> avps) {
            this.avps = avps;
        }
    }
}
