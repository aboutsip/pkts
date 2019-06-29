/**
 *
 */
package io.pkts.gtp;


import io.snice.buffer.Buffer;

/**
 * Just some wire dumps we use for testing our parsing with.
 *
 * @author jonas@jonasborjesson.com
 */
public class GtpRawData {

    /**
     * A GTPv2 Delete Bearer Request
     */
    public static final Buffer deleteBearerRequestGtpv2 = Buffer.of(
            (byte) 0x48, (byte) 0x63, (byte) 0x00, (byte) 0x0d, (byte) 0xa5, (byte) 0xd2, (byte) 0x68, (byte) 0xf0,
            (byte) 0x35, (byte) 0x3d, (byte) 0x09, (byte) 0x00, (byte) 0x49, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x05);

    /**
     * A GTPv2 Delete Bearer Response, which is the response to {@link #deleteBearerRequestGtpv2}
     */
    public static final Buffer deleteBearerResponseGtpv2 = Buffer.of(
            (byte) 0x48, (byte) 0x64, (byte) 0x00, (byte) 0x37, (byte) 0x57, (byte) 0xb5, (byte) 0x01, (byte) 0xf8,
            (byte) 0x35, (byte) 0x3d, (byte) 0x09, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x00,
            (byte) 0x10, (byte) 0x00, (byte) 0x72, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x21, (byte) 0x01,
            (byte) 0x56, (byte) 0x00, (byte) 0x0d, (byte) 0x00, (byte) 0x18, (byte) 0x82, (byte) 0xf0, (byte) 0x10,
            (byte) 0x04, (byte) 0x65, (byte) 0x82, (byte) 0xf0, (byte) 0x10, (byte) 0x00, (byte) 0x01, (byte) 0x5c,
            (byte) 0x03, (byte) 0xaa, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0xe0, (byte) 0xc0, (byte) 0x49,
            (byte) 0xa3, (byte) 0x49, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x05, (byte) 0x03, (byte) 0x00,
            (byte) 0x01, (byte) 0x00, (byte) 0x14);


    /**
     * A GTPv1 Create PDP Context Request
     */
    public static final Buffer createPdpContextRequest = Buffer.of(
            (byte) 0x32, (byte) 0x10, (byte) 0x00, (byte) 0xb4,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6a, (byte) 0xf3, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x32, (byte) 0x54, (byte) 0x70, (byte) 0x90, (byte) 0x12, (byte) 0x11, (byte) 0x46,
            (byte) 0xf9, (byte) 0x03, (byte) 0x22, (byte) 0xf6, (byte) 0x30, (byte) 0xff, (byte) 0xfe, (byte) 0xff,
            (byte) 0x0e, (byte) 0x77, (byte) 0x0f, (byte) 0xfc, (byte) 0x10, (byte) 0x17, (byte) 0xab, (byte) 0x15,
            (byte) 0xf2, (byte) 0x11, (byte) 0x17, (byte) 0xab, (byte) 0x15, (byte) 0xf2, (byte) 0x14, (byte) 0x05,
            (byte) 0x1a, (byte) 0x04, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x02, (byte) 0xf1, (byte) 0x21,
            (byte) 0x83, (byte) 0x00, (byte) 0x16, (byte) 0x08, (byte) 0x69, (byte) 0x6e, (byte) 0x74, (byte) 0x65,
            (byte) 0x72, (byte) 0x6e, (byte) 0x65, (byte) 0x74, (byte) 0x08, (byte) 0x63, (byte) 0x68, (byte) 0x65,
            (byte) 0x65, (byte) 0x72, (byte) 0x69, (byte) 0x6f, (byte) 0x74, (byte) 0x03, (byte) 0x63, (byte) 0x6f,
            (byte) 0x6d, (byte) 0x84, (byte) 0x00, (byte) 0x2b, (byte) 0x80, (byte) 0xc0, (byte) 0x23, (byte) 0x14,
            (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x14, (byte) 0x0e, (byte) 0x4e, (byte) 0x61, (byte) 0x6e,
            (byte) 0x6f, (byte) 0x50, (byte) 0x69, (byte) 0x2d, (byte) 0x4e, (byte) 0x45, (byte) 0x4f, (byte) 0x2d,
            (byte) 0x41, (byte) 0x69, (byte) 0x72, (byte) 0x00, (byte) 0x80, (byte) 0x21, (byte) 0x10, (byte) 0x01,
            (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x81, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x83, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x85,
            (byte) 0x00, (byte) 0x04, (byte) 0xb2, (byte) 0x8a, (byte) 0x81, (byte) 0x01, (byte) 0x85, (byte) 0x00,
            (byte) 0x04, (byte) 0xb2, (byte) 0x8a, (byte) 0x81, (byte) 0x03, (byte) 0x86, (byte) 0x00, (byte) 0x09,
            (byte) 0x91, (byte) 0x88, (byte) 0x32, (byte) 0x26, (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x46,
            (byte) 0xf9, (byte) 0x87, (byte) 0x00, (byte) 0x0c, (byte) 0x03, (byte) 0x23, (byte) 0x63, (byte) 0x1f,
            (byte) 0x93, (byte) 0x96, (byte) 0x58, (byte) 0x74, (byte) 0x74, (byte) 0x63, (byte) 0x10, (byte) 0x40,
            (byte) 0x94, (byte) 0x00, (byte) 0x01, (byte) 0x40, (byte) 0x97, (byte) 0x00, (byte) 0x01, (byte) 0x02,
            (byte) 0x99, (byte) 0x00, (byte) 0x02, (byte) 0x21, (byte) 0x21, (byte) 0x9a, (byte) 0x00, (byte) 0x08,
            (byte) 0x68, (byte) 0x87, (byte) 0x85, (byte) 0x30, (byte) 0x02, (byte) 0x28, (byte) 0x99, (byte) 0x87);
}
