/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.nio.ByteOrder;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class PcapRecordHeader {

    private ByteOrder byteOrder;

    private final Buffer body;

    /**
     * 
     */
    public PcapRecordHeader(final ByteOrder byteOrder, final Buffer body) {
        assert body != null;
        assert body.capacity() == 16;

        this.body = body;
    }

    public long getTimeStampSeconds() {
        return PcapGlobalHeader.getUnsignedInt(0, this.body.getArray(), this.byteOrder);
    }

    public long getTimeStampMicroSeconds() {
        return PcapGlobalHeader.getUnsignedInt(4, this.body.getArray(), this.byteOrder);
    }

    /**
     * Get the total length of the data. Not all of that data may have been
     * captured in this one frame, which is evident if the actual captured
     * length is different from the total length
     * 
     * @return
     */
    public long getTotalLength() {
        return PcapGlobalHeader.getUnsignedInt(8, this.body.getArray(), this.byteOrder);
    }

    /**
     * Get the actual length of what is contained in this frame.
     * 
     * @return the length in bytes
     */
    public long getCapturedLength() {
        return PcapGlobalHeader.getUnsignedInt(12, this.body.getArray(), this.byteOrder);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final long ts = getTimeStampSeconds();
        final long tsMicroSeconds = getTimeStampMicroSeconds();
        sb.append("ts_s: ").append(ts).append("\n");
        sb.append("ts_us: ").append(tsMicroSeconds).append("\n");
        sb.append("octects: ").append(getTotalLength()).append("\n");
        sb.append("length: ").append(getCapturedLength()).append("\n");

        return sb.toString();
    }
}
