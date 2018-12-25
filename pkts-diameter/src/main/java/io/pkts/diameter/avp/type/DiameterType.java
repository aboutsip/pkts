package io.pkts.diameter.avp.type;

import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.impl.DiameterIdentityAvp;

import java.util.stream.Stream;

public interface DiameterType {

    enum Type {

        OCTET_STRING("OctetString", null, null, null),
        INTEGER_32("Integer32", null, Integer32.class, null),
        INTEGER_64("Integer64", null, Integer64.class, null),
        UNSIGNED_32("Unsigned32", null, null, null),
        UNSIGNED_64("Unsigned64", null, null, null),
        FLOAT_32("Float32", null, null, null),
        FLOAT_64("Float64", null, null, null),
        TIME("Time", null, null, null),

        UTF8_STRING("UTF8String", OCTET_STRING, null, null),
        ENUMERATED("Enumerated", INTEGER_32, null, null),
        DIAMETER_URI("DiameterURI", UTF8_STRING, null, null),
        IP_ADDRESS("IPAddress", OCTET_STRING, null, null),
        DIAMETER_IDENTITY("DiameterIdentity", OCTET_STRING, DiameterIdentity.class, DiameterIdentityAvp.class),
        IP_FILTER_RULE("IPFilterRule", OCTET_STRING, null, null),
        QoS_FILTER_RULE("QoSFilterRule", OCTET_STRING, null, null),
        MIP_REGISTRATION_REQUEST("MIPRegistrationRequest", OCTET_STRING, null, null),
        VENDOR_ID("VendorId", UNSIGNED_32, null, null),
        APP_ID("AppId", UNSIGNED_32, null, null);

        public static Type fromName(final String name) {
            return Stream.of(Type.values())
                    .filter(t -> name.equalsIgnoreCase(t.name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No enum constant " + Type.class.getCanonicalName() + "." + name));
        }

        private final Class<? extends DiameterType> implementingInterface;
        private final Class<? extends Avp> implementingClass;
        private final Type parent;
        private final String name;

        public String getName() {
            return name;
        }

        public Class<? extends Avp> getImplementingClass() {
            return implementingClass;
        }

        public Class<? extends DiameterType> getImplementingInterface() {
            return implementingInterface;
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

        Type(final String name, final Type parent,
             final Class<? extends DiameterType> implementingInterface,
             final Class<? extends Avp> implementingClass) {
            this.implementingInterface = implementingInterface;
            this.implementingClass = implementingClass;
            this.name = name;
            this.parent = parent;
        }

        public Type getBaseType() {
            return parent != null ? parent : this;
        }

    }
}
