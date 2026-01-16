package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.WWWAuthenticateHeader;
import io.pkts.packet.sip.impl.SipParser;

import java.util.LinkedHashMap;
import java.util.Map;

public class WWWAuthenticateHeaderImpl extends SipHeaderImpl implements WWWAuthenticateHeader {


    private Map<Buffer, Buffer> paramMap = new LinkedHashMap<>();

    private Buffer realm;
    private Buffer nonce;
    private Buffer algorithm;
    private Buffer qop;


    /**
     * @param value
     */
    public WWWAuthenticateHeaderImpl(Buffer value) {
        super(WWWAuthenticateHeader.NAME, value);

        Buffer original = value.clone();
        Buffer params = null;
        if (original.hasReadableBytes()) {
            params = original.slice("Digest ".length(), original.getUpperBoundary());
        }

        final byte[] VALUE_END_1 = Buffers.wrap("\", ").getArray();
        final byte[] VALUE_END_2 = Buffers.wrap(", ").getArray();

        //WWW-Authenticate: Digest realm="10.32.26.25",
        // nonce="bee3366b-cf59-476e-bc5e-334e0d65b386",
        // algorithm=MD5,
        // qop="auth"

        try {
            // 思路：
            // 1 遇到[=]号是key结束，遇到[，]或[", ]或[\r\n]是value结束
            // 2 每次遇"="或”，”标识lastMarkIndex
            int lastMarkIndex = params.getReaderIndex();
            boolean inKey = true;
            Buffer latestKey = Buffers.EMPTY_BUFFER, latestValue;
            while (params.hasReadableBytes() && params.getReaderIndex() <= params.getUpperBoundary()) {
                if (inKey && SipParser.isNext(params, SipParser.EQ)) {
                    //遇到[=]认为key结束
                    latestKey = params.slice(lastMarkIndex, params.getReaderIndex());
                    params.setReaderIndex(params.getReaderIndex() + 1);
                    if (SipParser.isNext(params, SipParser.DQUOT)) {
                        //跳过[="]等号后的第1个双引号
                        params.setReaderIndex(params.getReaderIndex() + 1);
                        inKey = false;
                    }
                    lastMarkIndex = params.getReaderIndex();
                } else if (params.getReadableBytes() == 1 ||
                        SipParser.isNext(params, VALUE_END_1) ||
                        SipParser.isNext(params, VALUE_END_2)) {
                    //遇到[", ]或[, ]视为value结束
                    if (params.getReadableBytes() == 1 && params.peekByte() != SipParser.DQUOT) {
                        latestValue = params.slice(lastMarkIndex, params.getReaderIndex() + 1);
                    } else {
                        latestValue = params.slice(lastMarkIndex, params.getReaderIndex());
                    }

                    paramMap.put(latestKey, latestValue);

                    if (params.getReadableBytes() == 1) {
                        params.setReaderIndex(params.getReaderIndex() + 1);
                    } else if (SipParser.isNext(params, VALUE_END_1)) {
                        params.setReaderIndex(params.getReaderIndex() + VALUE_END_1.length);
                    } else if (SipParser.isNext(params, VALUE_END_2)) {
                        params.setReaderIndex(params.getReaderIndex() + VALUE_END_2.length);
                    }

                    lastMarkIndex = params.getReaderIndex();

                    inKey = true;
                } else {
                    params.setReaderIndex(params.getReaderIndex() + 1);
                }
            }
        } catch (Exception e) {
            throw new SipParseException(NAME + " parse error, " + e.getCause());
        }
    }


    public WWWAuthenticateHeaderImpl(Buffer realm, Buffer nonce, Buffer algorithm, Buffer qop) {
        super(WWWAuthenticateHeader.NAME, Buffers.EMPTY_BUFFER);
        this.realm = realm;
        this.nonce = nonce;
        this.algorithm = algorithm;
        this.qop = qop;
    }

    @Override
    public Buffer getValue() {
        Buffer value = super.getValue();
        if (value != null && value != Buffers.EMPTY_BUFFER) {
            return value;
        }
        StringBuilder sb = new StringBuilder("Digest realm=\"" + this.getRealm() + "\", nonce=\"" + this.getNonce() + "\"");
        if (this.getAlgorithm() != null) {
            sb.append(", algorithm=" + this.getAlgorithm());
        }
        if (this.getQop() != null) {
            sb.append(", qop=\"" + this.getQop() + "\"");
        }
        value = Buffers.wrap(sb.toString());
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(NAME.toString());
        sb.append(": Digest realm=\"" + this.getRealm() + "\", nonce=\"" + this.getNonce() + "\"");
        if (this.getAlgorithm() != null) {
            sb.append(", algorithm=" + this.getAlgorithm());
        }
        if (this.getQop() != null) {
            sb.append(", qop=\"" + this.getQop() + "\"");
        }
        return sb.toString();
    }


    @Override
    public WWWAuthenticateHeader.Builder copy() {
        return new WWWAuthenticateHeader.Builder(getValue());
    }

    @Override
    public WWWAuthenticateHeader ensure() {
        return this;
    }

    @Override
    public WWWAuthenticateHeader clone() {
        final Buffer value = getValue();
        return new WWWAuthenticateHeaderImpl(value.clone());
    }

    @Override
    public Buffer getRealm() {
        if (realm != null) {
            return realm;
        }
        realm = paramMap.get(Buffers.wrap("realm"));
        return realm;
    }

    @Override
    public Buffer getNonce() {
        if (nonce != null) {
            return nonce;
        }
        nonce = paramMap.get(Buffers.wrap("nonce"));
        return nonce;
    }

    @Override
    public Buffer getAlgorithm() {
        if (algorithm != null) {
            return algorithm;
        }
        algorithm = paramMap.get(Buffers.wrap("algorithm"));
        return algorithm;
    }

    @Override
    public Buffer getQop() {
        if (qop != null) {
            return qop;
        }
        qop = paramMap.get(Buffers.wrap("qop"));
        return qop;
    }
}
