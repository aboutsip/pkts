package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.WWWAuthenticateHeaderImpl;


public interface WWWAuthenticateHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("WWW-Authenticate");

    Buffer getRealm();

    Buffer getNonce();

    Buffer getAlgorithm();

    Buffer getQop();
    
    static WWWAuthenticateHeader frame(final Buffer buffer) throws SipParseException {
        try {
            return new WWWAuthenticateHeader.Builder(buffer).build();
        } catch (final Exception e) {
            throw new SipParseException(0, "Unable to frame the WWWAuthenticate header due to IOException", e);
        }
    }

    @Override
    default WWWAuthenticateHeader toWWWAuthenticateHeader() {
        return this;
    }


    class Builder implements SipHeader.Builder<WWWAuthenticateHeader> {
        private Buffer value;

        private Buffer realm;
        private Buffer nonce;
        private Buffer algorithm;
        private Buffer qop;

        public Builder() {

        }

        public Builder(Buffer value) {
            this.value = value;
        }

        @Override
        public WWWAuthenticateHeader.Builder withValue(Buffer value) {
            this.value = value;
            return this;
        }

        public WWWAuthenticateHeader.Builder withRealm(Buffer realm) {
            this.realm = realm;
            return this;
        }

        public WWWAuthenticateHeader.Builder withNonce(Buffer nonce) {
            this.nonce = nonce;
            return this;
        }

        public WWWAuthenticateHeader.Builder withAlgorithm(Buffer algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public WWWAuthenticateHeader.Builder withQop(Buffer qop) {
            this.qop = qop;
            return this;
        }

        @Override
        public WWWAuthenticateHeader build() throws SipParseException {
            if (value == null &&
                    (this.realm == null && this.nonce == null)) {
                throw new SipParseException("You must specify the [value] or [realm/nonce] of the WWWAuthenticate-Header");
            }

            if (this.value != null) {
                return new WWWAuthenticateHeaderImpl(value);
            } else {
                return new WWWAuthenticateHeaderImpl(realm, nonce, algorithm, qop);
            }
        }
    }

}
