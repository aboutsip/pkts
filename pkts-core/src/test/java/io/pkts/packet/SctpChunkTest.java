package io.pkts.packet;

import io.pkts.packet.sctp.SctpChunk;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SctpChunkTest {

    /**
     * Just to ensure we haven't copy/pasted the wrong value of the actual type.
     * So make sure it is only defined once.
     */
    @Test
    public void testChunkType() {
        final int[] count = new int[255];
        final SctpChunk.Type[] types = SctpChunk.Type.values();
        for (int i = 0; i < types.length; ++i) {
            count[types[i].getType()] = count[types[i].getType()] + 1;
        }

        for (int i = 0; i < count.length; ++i) {
            assertThat("Found multiple type definitions for Chunk Type " + i, count[i] < 2, is(true));
        }

    }

}