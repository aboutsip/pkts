/**
 * 
 */
package io.pkts.diameter;

import io.pkts.buffer.Buffers;
import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.impl.DiameterParser;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.pkts.diameter.impl.DiameterParser.couldBeDiameterMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterTestBase {

    /**
     * These are "raw" AVPs that we have stored in raw files based off of exporting them from wireshark.
     */
    public static final RawAvpHolder[] RAW_AVPS = new RawAvpHolder[]{
            new RawAvpHolder("AVP_Subscription_Id.raw", 443, 44, Optional.empty(), false, true, false),
            new RawAvpHolder("AVP_PDN_Connection_Charging_ID.raw", 2050, 16, Optional.of(10415L), true, true, false),
            new RawAvpHolder("AVP_3GPP_SGSN_MCC_MNC.raw", 18, 17, Optional.of(10415L), true, false, false),
            new RawAvpHolder("AVP_Framed_IP_Address.raw", 8, 12, Optional.empty(), false, true, false),
            new RawAvpHolder("AVP_PDN_Connection_ID.raw", 1065, 13, Optional.of(10415L), true, false, false),
    };

    /**
     * These are the "raw" diameter messages that came from the diameter.pcap, as checked in
     * along side these raw version. They are simply just extractions from that pcap as raw bytes
     * and then the associated values have been verified using wireshark.
     */
    public static final RawDiameterMessageHolder[] RAW_DIAMETER_MESSAGES = new RawDiameterMessageHolder[]{
            new RawDiameterMessageHolder("001_diameter_auth_info_request.raw", 344, true, true, false, false, 318, 16777251L, 0x6cac28cc, 0xfb1329de, 9),
            new RawDiameterMessageHolder("002_diameter_auth_info_answer.raw", 832, false, true, false, false, 318, 16777251L, 0x6cac28cc, 0xfb1329de, 7),
            new RawDiameterMessageHolder("003_diameter_update_location_request.raw", 456, true, true, false, false, 316, 16777251L, 0x6cac28cd, 0xfb1329df, 15),
            new RawDiameterMessageHolder("004_diameter_update_location_answer.raw", 1024, false, true, false, false, 316, 16777251L, 0x6cac28cd, 0xfb1329df, 8),
            new RawDiameterMessageHolder("005_diameter_credit_control_request.raw", 664, true, true, false, false, 272, 16777238L, 0x12681e58, 0x0636af13, 22),
            new RawDiameterMessageHolder("006_diameter_credit_control_answer.raw", 252, false, true, false, false, 272, 16777238L, 0x12681e58, 0x0636af13, 9),
            new RawDiameterMessageHolder("007_diameter_notify_request.raw", 316, true, true, false, false, 323, 16777251L, 0x6cac28ce, 0xfb1329e0, 9),
            new RawDiameterMessageHolder("008_diameter_notify_answer.raw", 228, false, true, false, false, 323, 16777251L, 0x6cac28ce, 0xfb1329e0, 6),
            new RawDiameterMessageHolder("009_diameter_purge_ue_request.raw", 284, true, true, false, false, 321, 16777251L, 0x6cac28cf, 0xfb1329e1, 7),
            new RawDiameterMessageHolder("010_diameter_credit_control_request.raw", 420, true, true, false, false, 272, 16777238L, 0x12681e59, 0x0636af14, 14),
            new RawDiameterMessageHolder("011_diameter_purge_ue_answer.raw", 244, false, true, false, false, 321, 16777251L, 0x6cac28cf, 0xfb1329e1, 7),
            new RawDiameterMessageHolder("012_diameter_credit_control_answer.raw", 220, false, true, false, false, 272, 16777238L, 0x12681e59, 0x0636af14, 7),
            new RawDiameterMessageHolder("013_diameter_device_watchdog_request.raw", 112, true, false, false, false, 280, 0L, 0x12681e5b, 0x0636af14, 2),
            new RawDiameterMessageHolder("014_diameter_device_watchdog_answer.raw", 124, false, false, false, false, 280, 0L, 0x12681e5b, 0x0636af14, 3)
    };

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    public static ReadOnlyBuffer loadBuffer(final String resource) throws Exception {
        final Path path = Paths.get(DiameterTestBase.class.getResource(resource).toURI());
        final File file = path.toFile();
        final byte[] buffer = new byte[(int)file.length()];
        final InputStream ios = new FileInputStream(path.toFile());
        ios.read(buffer);
        return Buffers.wrapAsReadOnly(buffer);
    }

    public static DiameterMessage loadDiameterMessage(final String resource) throws Exception {
        final ReadOnlyBuffer buffer = loadBuffer(resource);
        System.out.println(buffer.toString());
        return DiameterMessage.frame(buffer);

    }

    public static class RawAvpHolder {

        public final String resource;

        public final long code;

        public final int length;

        public final Optional<Long> vendorId;

        public final boolean isVendor;
        public final boolean isMandatory;
        public final boolean isProtected;

        public RawAvpHolder(final String resource, final long code, final int length, final Optional<Long> vendorId, final boolean isVendor, final boolean isMandatory, final boolean isProtected) {
            this.resource = resource;
            this.code = code;
            this.length = length;
            this.vendorId = vendorId;
            this.isVendor = isVendor;
            this.isMandatory = isMandatory;
            this.isProtected = isProtected;
        }

        /**
         * Convenience method for checking that the supplied header matches what we have in the
         * raw format.
         */
        public void assertHeader(final AvpHeader header) {
            assertThat("Incorrect AVP Code for resource " + resource, header.getCode(), is(code));
            assertThat("Incorrect length for resource " + resource, header.getLength(), is(length));

            assertThat("Incorrect vendor id for resource " + resource, header.getVendorId(), is(vendorId));

            assertThat("Incorrect vendor specific bit for resource " + resource, header.isVendorSpecific(), is(isVendor));
            assertThat("Incorrect mandatory bit for resource " + resource, header.isMandatory(), is(isMandatory));
            assertThat("Incorrect protected bit for resource " + resource, header.isProtected(), is(isProtected));
        }

        public ReadOnlyBuffer load() {
            try {
                return loadBuffer(resource);
            } catch (final Exception e) {
                throw new RuntimeException("Issue loading the raw AVP byte-array from resource " + resource, e);
            }
        }

        public AvpHeader getHeader() {
            try {
                return AvpHeader.frame(load());
            } catch (final Exception e) {
                throw new RuntimeException("Issue loading the raw diameter byte-array from resource " + resource, e);
            }
        }

        public Avp getAvp() {
            try {
                return Avp.frame(load());
            } catch (final Exception e) {
                throw new RuntimeException("Issue loading the raw diameter byte-array from resource " + resource, e);
            }
        }
    }

    /**
     * Simple class to hold information about the raw diameter messages
     * that we have checked in. We use these when unit testing etc and all
     * the values within have been verified using wireshark.
     */
    public static class RawDiameterMessageHolder {

        public final String resource;

        /**
         * This is the length of the diameter message as specified in the diameter header.
         */
        public final int length;

        /**
         * Indicates if this is a request or not. This is, again, what is part of the diameter header
         * and we have verified this value using wireshark manually.
         * <p>
         * This would be part of the Command Flags in the 5th byte of the Diameter header
         */
        public final boolean isRequest;

        /**
         * Flag in the Command Header indicating if this is proxiable or not.
         */
        public final boolean isProxiable;

        /**
         * Flag in the Command Header indicating if this is an error or not.
         */
        public final boolean isError;

        /**
         * Flag in the Command Header indicating if this is a potential retransmission.
         */
        public final boolean isRetransmit;

        /**
         * The command code
         */
        public final int commandCode;

        public final long applicationId;

        public final long hopByHopId;

        public final long endToEndId;

        /**
         * How many AVPs we expect in the message.
         */
        public final int avpCount;


        public RawDiameterMessageHolder(final String resource,
                                        final int length,
                                        final boolean isRequest,
                                        final boolean isProxiable,
                                        final boolean isError,
                                        final boolean isRetransmit,
                                        final int commandCode,
                                        final long applicationId,
                                        final long hopByHopId,
                                        final long endToEndId,
                                        final int avpCount) {
            this.resource = resource;
            this.length = length;
            this.isRequest = isRequest;
            this.isProxiable = isProxiable;
            this.isError = isError;
            this.isRetransmit = isRetransmit;
            this.commandCode = commandCode;
            this.applicationId = applicationId;
            this.hopByHopId = hopByHopId;
            this.endToEndId = endToEndId;
            this.avpCount = avpCount;
        }

        /**
         * Convenience method for checking that the supplied header matches what we have in the
         * raw format.
         *
         * @param header
         */
        public void assertHeader(final DiameterHeader header) throws Exception {
            assertThat("Incorrect lenght for resource " + resource, header.getLength(), is(length));

            // also making sure that we don't accidently do something stupid when it comes to the
            // header.isResponse() - i.e., accidentally remove the not flag or something silly.
            assertThat("Incorrect request/response marker bit for resource " + resource, header.isRequest(), is(isRequest));
            assertThat("Incorrect request/response marker bit for resource " + resource, header.isResponse(), is(!isRequest));

            assertThat("Incorrect proxiable marker bit for resource " + resource, header.isProxiable(), is(isProxiable));
            assertThat("Incorrect error marker bit for resource " + resource, header.isError(), is(isError));
            assertThat("Incorrect re-transmission marker bit for resource " + resource, header.isPossiblyRetransmission(), is(isRetransmit));
            assertThat("Incorrect Command Code for resource " + resource, header.getCommandCode(), is(commandCode));

            assertThat("Incorrect Application Id for resource " + resource, header.getApplicationId(), is(applicationId));

            // also, all of the above are valid diameter messages with valid
            // diameter headers so make sure that our parser also thinks so
            assertTrue(resource + " not recognized as a containing a valid diameter message", couldBeDiameterMessage(load()));

        }

        /**
         * Convenience method for loading the associated resource as a raw buffer.
         * <p>
         * Note, throwing {@link RuntimeException} here instead because we're using
         * this stuff in lambdas etc and don't want to do catch-stuff for it. If it
         * fails the unit test will fail so all good.
         */
        public ReadOnlyBuffer load() {
            try {
                return loadBuffer(resource);
            } catch (final Exception e) {
                throw new RuntimeException("Issue loading the raw diameter byte-array from resource " + resource, e);
            }
        }

        /**
         * Convenience method for loading and extracting out the {@link DiameterHeader}
         * from the associated resource.
         * <p>
         */
        public DiameterHeader getHeader() {
            try {
                return DiameterParser.frameHeader(load());
            } catch (final Exception e) {
                throw new RuntimeException("Issue loading the raw diameter byte-array from resource " + resource, e);
            }
        }

    }
}
