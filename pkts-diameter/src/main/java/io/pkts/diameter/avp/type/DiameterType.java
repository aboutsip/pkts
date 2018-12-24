package io.pkts.diameter.avp.type;

import java.util.stream.Stream;

public interface DiameterType {

    enum Type {

        OCTET_STRING("OctetString", null),
        INTEGER_32("Integer32", null),
        INTEGER_64("Integer64", null),
        UNSIGNED_32("Unsigned32", null),
        UNSIGNED_64("Unsigned64", null),
        FLOAT_32("Float32", null),
        FLOAT_64("Float64", null),
        TIME("Time", null),

        UTF8_STRING("UTF8String", OCTET_STRING),
        ENUMERATED("Enumerated", INTEGER_32),
        DIAMETER_URI("DiameterURI", UTF8_STRING),
        IP_ADDRESS("IPAddress", OCTET_STRING),
        DIAMETER_IDENTITY("DiameterIdentity", OCTET_STRING),
        IP_FILTER_RULE("IPFilterRule", OCTET_STRING),
        QoS_FILTER_RULE("QoSFilterRule", OCTET_STRING),
        MIP_REGISTRATION_REQUEST("MIPRegistrationRequest", OCTET_STRING),
        VENDOR_ID("VendorId", UNSIGNED_32),
        APP_ID("AppId", UNSIGNED_32);

        public static Type fromName(final String name) {
            return Stream.of(Type.values())
                    .filter(t -> name.equalsIgnoreCase(t.name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No enum constant " + Type.class.getCanonicalName() + "." + name));
        }

        private final Type parent;
        private final String name;

        public String getName() {
            return name;
        }

        public boolean isUnsigned32() {
            return this == UNSIGNED_32;
        }

        public boolean isInteger32() {
            return this == INTEGER_32;
        }

        public boolean isEnumerated() {
            return this == ENUMERATED;
        }

        Type(final String name, final Type parent) {
            this.name = name;
            this.parent = parent;
        }

        public Type getBaseType() {
            return parent != null ? parent : this;
        }

    }
}
