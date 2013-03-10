/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.header.impl.SipHeaderImpl;

/**
 * Basic sip parser that contains most (all?) of the different grammar rules for
 * SIP as defined by RFC 3261. View these various methods as building blocks for
 * building up a complete SIP parser (perhaps I should rename this class?).
 * 
 * All of these functions work in the following way:
 * <ul>
 * <li><b>consumeXXXX</b> - will (in general) simply try and consume whatever it
 * is supposed to consume and the function will return true of false depending
 * on whether is was able to consume anything from the {@link Buffer}. The
 * consume-functions will (if successful) move the reader index of the
 * {@link Buffer}. If unsuccessful, the reader index will be left untouched.</li>
 * <li><b>expectXXX</b> - will (in general) expect that the next thing is
 * whatever it is supposed to expect. These functions are really the same as the
 * consumeXXXX ones but instead of returning true or false the expect-functions
 * will throw a {@link SipParseException} to indicate that things didn't turn
 * out as we were hoping for. Also, remember that the {@link SipParseException}
 * contains the error offset into the {@link Buffer} where things broke. As with
 * the consume-functions, the expect-functions will (if successful) move the
 * reader index of the {@link Buffer}</li>
 * <li></li>
 * </ul>
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipParser {

    public static final Buffer SIP2_0 = Buffers.wrap("SIP/2.0");

    public static final byte COLON = ':';

    public static final byte SEMI = ';';

    public static final byte CR = '\r';

    public static final byte LF = '\n';

    public static final byte SP = ' ';

    public static final byte HTAB = '\t';

    public static final byte DASH = '-';

    public static final byte PERIOD = '.';

    public static final byte COMMA = ',';

    public static final byte EXCLAMATIONPOINT = '!';

    public static final byte PERCENT = '%';

    public static final byte STAR = '*';

    public static final byte UNDERSCORE = '_';

    public static final byte QUESTIONMARK = '?';

    public static final byte PLUS = '+';

    public static final byte BACKTICK = '`';

    public static final byte TICK = '\'';

    public static final byte TILDE = '~';

    public static final byte EQ = '=';

    public static final byte SLASH = '/';

    public static final byte BACK_SLASH = '\\';

    /**
     * Left parenthesis
     */
    public static final byte LPAREN = '(';

    /**
     * Right parenthesis
     */
    public static final byte RPAREN = ')';

    /**
     * Right angle quote
     */
    public static final byte RAQUOT = '>';

    /**
     * Left angle quote
     */
    public static final byte LAQUOT = '<';

    /**
     * Double quotation mark
     */
    public static final byte DQUOT = '"';

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // -------- Expect methods expects something to be true and if not ------
    // -------- they will throw an exception of some sort -------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Expect that the next set of bytes is "SIP/2.0" and if not then we will
     * throw a
     * 
     * @param buffer
     */
    public static void expectSIP2_0(final Buffer buffer) throws SipParseException {
        expect(buffer, 'S');
        expect(buffer, 'I');
        expect(buffer, 'P');
        expect(buffer, '/');
        expect(buffer, '2');
        expect(buffer, '.');
        expect(buffer, '0');
    }

    /**
     * 
     * @return
     */
    public static Buffer expectMethod(final Buffer buffer) {
        return null;
    }

    /**
     * Consumes all generic-params it can find. If there are no generic params
     * to be consumed it will return an empty list. The list of generic-params
     * are found on many SIP headers such as the To, From etc. In general, the
     * BNF looks like so:
     * 
     * 
     * <pre>
     *  *( SEMI generic-param )
     * </pre>
     * 
     * E.g. in To:
     * 
     * <pre>
     * To        =  ( "To" / "t" ) HCOLON ( name-addr / addr-spec ) *( SEMI to-param )
     * to-param  =  tag-param / generic-param
     * </pre>
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static List<Buffer[]> consumeGenericParams(final Buffer buffer) throws IndexOutOfBoundsException,
    IOException {
        final List<Buffer[]> params = new ArrayList<Buffer[]>();
        while (buffer.hasReadableBytes() && (buffer.peekByte() == SEMI)) {
            buffer.readByte(); // consume the SEMI
            params.add(consumeGenericParam(buffer));
        }
        return params;
    }


    /**
     * Consumes a generic param, which according to RFC 3261 section 25.1 is:
     * 
     * <pre>
     * generic-param  =  token [ EQUAL gen-value ]
     * gen-value      =  token / host / quoted-string
     * </pre>
     * 
     * The return value is two buffers returned as an array and if the second
     * element is null (so make sure you check it!) then there was no value for
     * this parameter.
     * 
     * Also note that due to poor implementations out there, the following is
     * accepted as valid input:
     * 
     * foo=
     * 
     * I.e., foo is a flag parameter and as such there should not be an equal
     * sign following it but there are many servers out there that does this
     * anyway.
     * 
     * @param buffer
     *            the buffer from which we will consume a generic-param
     * @return a buffer array of size two. The first element is the name of the
     *         parameter and the second element is the value of that parameter
     *         or null if the parameter was a flag parameter (didn't have a
     *         value)
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static Buffer[] consumeGenericParam(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        final Buffer key = consumeToken(buffer);
        Buffer value = null;
        if (key == null) {
            return new Buffer[2];
        }
        if (consumeEQUAL(buffer) > 0) {
            // TODO: consume host and quoted string
            value = consumeToken(buffer);
        }
        return new Buffer[] {
                key, value };
    }


    /**
     * Will check whether the next readable byte in the buffer is a certain byte
     * 
     * @param buffer the buffer to peek into
     * @param b the byte we are checking is the next byte in the buffer to be
     *            read
     * @return true if the next byte to read indeed is what we hope it is
     */
    public static boolean isNext(final Buffer buffer, final byte b) throws IOException {
        if (buffer.hasReadableBytes()) {
            final Byte actual = buffer.peekByte();
            return actual == b;
        }

        return false;
    }

    /**
     * Check whether the next byte is a digit or not
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static boolean isNextDigit(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        if (buffer.hasReadableBytes()) {
            final char next = (char) buffer.peekByte();
            return (next >= 48) && (next <= 57);
        }

        return false;
    }

    /**
     * Will expect at least 1 digit and will continue consuming bytes until a
     * non-digit is encountered
     * 
     * @param buffer the buffer to consume the digits from
     * @return the buffer containing digits only
     * @throws SipParseException in case there is not one digit at the first
     *             position in the buffer (where the current reader index is
     *             pointing to)
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static Buffer expectDigit(final Buffer buffer) throws SipParseException {
        final int start = buffer.getReaderIndex();
        try {

            while (buffer.hasReadableBytes() && isNextDigit(buffer)) {
                // consume it
                buffer.readByte();
            }

            if (start == buffer.getReaderIndex()) {
                throw new SipParseException(start, "Expected digit");
            }

            return buffer.slice(start, buffer.getReaderIndex());
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(start, "Expected digit but no more bytes to read");
        } catch (final IOException e) {
            throw new SipParseException(start, "Expected digit unable to read from underlying stream");
        }
    }

    /**
     * Convenience method for expecting (and consuming) a HCOLON, which is
     * defined as:
     * 
     * HCOLON = *( SP / HTAB ) ":" SWS
     * 
     * See RFC3261 section 25.1 Basic Rules
     */
    public static int expectHCOLON(final Buffer buffer) throws SipParseException {
        try {
            int consumed = consumeWS(buffer);
            expect(buffer, COLON);
            ++consumed;
            consumed += consumeSWS(buffer);
            return consumed;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Convenience method for expecting (and consuming) a SLASH, which is
     * defined as:
     * 
     * 
     * See RFC3261 section 25.1 Basic Rules
     * 
     * @param buffer
     * @throws SipParseException
     */
    public static void expectSLASH(final Buffer buffer) throws SipParseException {
        try {
            final int count = consumeSLASH(buffer);
            if (count == 0) {
                throw new SipParseException(buffer.getReaderIndex(), "Expected SLASH");
            }
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected SLASH but nothing more to read", e);

        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected SLASH but problem reading from stream", e);
        }
    }

    /**
     * Consume an slash (SLASH), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * SLASH = SWS "/" SWS ; slash
     * 
     * @param buffer
     * @return true if we indeed did consume a SLASH
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeSLASH(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, SLASH);
    }

    /**
     * Expect the next byte to be a white space
     * 
     * @param buffer
     */
    public static int expectWS(final Buffer buffer) throws SipParseException {
        int consumed = 0;
        try {
            if (buffer.hasReadableBytes()) {
                final byte b = buffer.getByte(buffer.getReaderIndex());
                if ((b == SP) || (b == HTAB)) {
                    // ok, it was a WS so consume the byte
                    buffer.readByte();
                    ++consumed;
                } else {
                    throw new SipParseException(buffer.getReaderIndex(), "Expected WS");
                }
            } else {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Expected WS but nothing more to read in the buffer");
            }
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
        return consumed;
    }

    /**
     * Consume sep (separating?) whitespace (LWS), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * SWS = [LWS] ; sep whitespace
     * 
     * "The SWS construct is used when linear white space is optional, generally
     * between tokens and separators." (RFC3261)
     * 
     * @param buffer
     * @return
     */
    public static int consumeSWS(final Buffer buffer) {
        try {
            return consumeLWS(buffer);
        } catch (final SipParseException e) {
            // ignore since currently the consumeLWS will ONLY
            // throw this exception when it expected a WS at a
            // particular place and since SWS is all optional
            // we will silently just consume it, but return
            // false however
            return 0;
        }
    }


    /**
     * Consume an asterisk/star (STAR), which according to RFC3261 section 25.1
     * Basic Rules is:
     * 
     * STAR = SWS "*" SWS ; asterisk
     * 
     * @param buffer
     * @return true if we indeed did consume a STAR
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeSTAR(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, STAR);
    }

    /**
     * Consume equal sign (EQUAL), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * EQUAL = SWS "=" SWS ; equal
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeEQUAL(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, EQ);
    }

    /**
     * Consume left parenthesis (LPAREN), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * LPAREN = SWS "(" SWS ; left parenthesis
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeLPAREN(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, LPAREN);
    }

    /**
     * Consume right parenthesis (RPAREN), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * RPAREN = SWS ")" SWS ; right parenthesis
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeRPAREN(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, RPAREN);
    }

    /**
     * Consume right angle quote (RAQUOT), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * RAQUOT = SWS ">"; left angle quote
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeRAQUOT(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, RAQUOT);
    }

    /**
     * Consume left angle quote (LAQUOT), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * LAQUOT  =  SWS "<"; left angle quote
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeLAQUOT(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, LAQUOT);
    }

    /**
     * Consume comma (COMMA), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * COMMA = SWS "," SWS ; comma
     * 
     * @param buffer
     * @return true if we indeed did consume a COMMA
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeCOMMA(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, COMMA);
    }

    /**
     * Consume semicolon (SEMI), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * SEMI = SWS ";" SWS ; semicolon
     * 
     * @param buffer
     * @return true if we indeed did consume a SEMI
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeSEMI(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, SEMI);
    }

    /**
     * Consume colon (COLON), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * COLON = SWS ":" SWS ; colon
     * 
     * @param buffer
     * @return true if we indeed did consume a COLON
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeCOLON(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, COLON);
    }

    /**
     * Consume open double quotation mark (LDQUT), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * LDQUOT = SWS DQUOTE; open double quotation mark
     * 
     * @param buffer
     * @return true if we indeed did consume a LDQUOT
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeLDQUOT(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        buffer.markReaderIndex();
        int consumed = consumeSWS(buffer);
        if (isNext(buffer, DQUOT)) {
            buffer.readByte();
            ++consumed;
        } else {
            buffer.resetReaderIndex();
            return 0;
        }
        return consumed;
    }

    /**
     * Consume close double quotation mark (RDQUT), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * RDQUOT = DQUOTE SWS ; close double quotation mark
     * 
     * @param buffer
     * @return true if we indeed did consume a LDQUOT
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeRDQUOT(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        buffer.markReaderIndex();
        int consumed = 0;
        if (isNext(buffer, DQUOT)) {
            buffer.readByte();
            ++consumed;
        } else {
            buffer.resetReaderIndex();
            return 0;
        }
        consumed += consumeSWS(buffer);
        return consumed;
    }

    /**
     * Helper function for checking stuff as described below. It is all the same pattern so...
     * (from rfc 3261 section 25.1)
     * 
     * When tokens are used or separators are used between elements,
     * whitespace is often allowed before or after these characters:
     * 
     * STAR    =  SWS "*" SWS ; asterisk
     * SLASH   =  SWS "/" SWS ; slash
     * EQUAL   =  SWS "=" SWS ; equal
     * LPAREN  =  SWS "(" SWS ; left parenthesis
     * RPAREN  =  SWS ")" SWS ; right parenthesis
     * RAQUOT  =  ">" SWS ; right angle quote
     * LAQUOT  =  SWS "<"; left angle quote
     * COMMA   =  SWS "," SWS ; comma
     * SEMI    =  SWS ";" SWS ; semicolon
     * COLON   =  SWS ":" SWS ; colon
     * LDQUOT  =  SWS DQUOTE; open double quotation mark
     * RDQUOT  =  DQUOTE SWS ; close double quotation mark

     * @param buffer
     * @param b
     * @return the number of bytes that was consumed.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    private static int consumeSeparator(final Buffer buffer, final byte b) throws IndexOutOfBoundsException,
    IOException {
        buffer.markReaderIndex();
        int consumed = consumeSWS(buffer);
        if (isNext(buffer, b)) {
            buffer.readByte();
            ++consumed;
        } else {
            buffer.resetReaderIndex();
            return 0;
        }
        consumed += consumeSWS(buffer);
        return consumed;
    }

    /**
     * Expects a token, which according to RFC3261 section 25.1 Basic Rules is:
     * 
     * token = 1*(alphanum / "-" / "." / "!" / "%" / "*" / "_" / "+" / "`" / "'"
     * / "~" )
     * 
     * @param buffer
     * @return the buffer containing the expected token
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @throws SipParseException
     *             in case there is no token
     */
    public static Buffer expectToken(final Buffer buffer) throws IndexOutOfBoundsException, IOException,
    SipParseException {
        final Buffer token = consumeToken(buffer);
        if (token == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected TOKEN");
        }
        return token;
    }

    /**
     * Consumes a quoted-string, which is defined as:
     * 
     * <pre>
     * quoted-string  =  SWS DQUOTE *(qdtext / quoted-pair ) DQUOTE
     * qdtext         =  LWS / %x21 / %x23-5B / %x5D-7E / UTF8-NONASCII
     * quoted-pair    =  "\" (%x00-09 / %x0B-0C / %x0E-7F)
     * </pre>
     * 
     * Note, this is a somewhat simplified version and we'll
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws SipParseException
     */
    public static Buffer consumeQuotedString(final Buffer buffer) throws SipParseException, IOException {
        final int start = buffer.getReaderIndex();
        expect(buffer, DQUOT);
        while (buffer.hasReadableBytes()) {
            final byte b = buffer.readByte();
            if (b == DQUOT) {
                break;
            } else if (b == BACK_SLASH) {
                buffer.readByte();
            }
        }
        final int stop = buffer.getReaderIndex();
        buffer.setReaderIndex(start + 1);
        final Buffer result = buffer.readBytes(stop - start - 2);
        buffer.setReaderIndex(stop);
        return result;
    }

    /**
     * The display name in SIP is a little tricky since it may or may not be
     * there and the stuff following it (whether or not it was there to begin
     * with) can easily be confused with being a display name.
     * 
     * Note, a display name in SIP is only part of of the "name-addr" construct
     * and this function assumes that (even though it actually would work like a
     * regular {@link #consumeToken(Buffer)} in some cases)
     * 
     * <pre>
     * name-addr      =  [ display-name ] LAQUOT addr-spec RAQUOT
     * display-name   =  *(token LWS)/ quoted-string
     * </pre>
     * 
     * @param buffer
     * @return the display name or null if there was none
     * @throws IOException
     * @throws SipParseException
     */
    public static Buffer consumeDisplayName(final Buffer buffer) throws IOException, SipParseException {
        if (isNext(buffer, DQUOT)) {
            return consumeQuotedString(buffer);
        }

        final int count = getTokenCount(buffer);
        if (count == 0) {
            return Buffers.EMPTY_BUFFER;
        }

        // now, if the next thing after the count is a ':' then
        // we mistook the scheme for a token so bail out.
        buffer.markReaderIndex();
        final Buffer potentialDisplayName = buffer.readBytes(count);
        if (isNext(buffer, COLON)) {
            buffer.resetReaderIndex();
            return Buffers.EMPTY_BUFFER;
        }

        // all good...
        return potentialDisplayName;
    }

    /**
     * Consumes addr-spec, which according to RFC3261 section 25.1 is:
     * 
     * <pre>
     * addr-spec      =  SIP-URI / SIPS-URI / absoluteURI
     * SIP-URI          =  "sip:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * SIPS-URI         =  "sips:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * 
     * absoluteURI    =  scheme ":" ( hier-part / opaque-part )
     * </pre>
     * 
     * And as you can see, it gets complicated. Also, these consume-functions
     * are not to validate the exact grammar but rather to find the boundaries
     * so the strategy for consuming the addr-spec is:
     * <ul>
     * <li>If '>' is encountered, then we assume that this addr-spec is within a
     * name-addr so we stop here</li>
     * <li>If a white space or end-of-line is encountered, we also assume we are
     * done.</li>
     * </ul>
     * 
     * Note, I think the above is safe since I do believe you cannot have a
     * white space or a quoted string (which could have contained white space)
     * within any of the elements that are part of the addr-spec... Anyone?
     * 
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @throws SipParseException
     *             in case we cannot successfully frame the addr-spec.
     */
    public static Buffer consumeAddressSpec(final Buffer buffer) throws IndexOutOfBoundsException, IOException,
    SipParseException {
        buffer.markReaderIndex();
        int count = 0;
        int state = 0; // zero is to look for colon, everything else is to find the end
        boolean done = false;

        while (buffer.hasReadableBytes() && !done) {
            ++count;
            final byte b = buffer.readByte();

            // emergency breaks...
            if ((state == 0) && (count > 99)) {
                throw new SipParseException(buffer.getReaderIndex(), "No scheme found after 100 bytes, giving up");
            } else if (count > 999) {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Have not been able to find the entire addr-spec after 1000 bytes, giving up");
            } else if ((state == 0) && (b == COLON)) {
                state = 1;
            } else if ((state == 1) && ((b == RAQUOT) || (b == SP) || (b == HTAB) || (b == CR) || (b == LF))) {
                done = true;
                --count;
            }
        }
        buffer.resetReaderIndex();

        // didn't find the scheme portion
        if (state == 0) {
            throw new SipParseException(buffer.getReaderIndex(), "No scheme found");
        }

        if (count > 0) {
            return buffer.readBytes(count);
        }

        return null;
    }

    /**
     * Consume a token, which according to RFC3261 section 25.1 Basic Rules is:
     * 
     * token = 1*(alphanum / "-" / "." / "!" / "%" / "*" / "_" / "+" / "`" / "'"
     * / "~" )
     * 
     * @param buffer
     * @return the buffer containing the token we consumed or null if nothing
     *         was consumed.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static Buffer consumeToken(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        final int count = getTokenCount(buffer);
        if (count == 0) {
            return null;
        }
        return buffer.readBytes(count);
    }

    /**
     * Consume a m-type, which according to RFC3261 section 25.1 Basic Rules is:
     * 
     * <pre>
     * m-type           =  discrete-type / composite-type
     * discrete-type    =  "text" / "image" / "audio" / "video" / "application" / extension-token
     * composite-type   =  "message" / "multipart" / extension-token
     * extension-token  =  ietf-token / x-token
     * ietf-token       =  token
     * x-token          =  "x-" token
     * </pre>
     * 
     * And if you really read through all of that stuff it all boils down to
     * "token" so that is all we end up doing in this method. Hence, this method
     * is really only to put some context to consuming a media-type
     * 
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public static Buffer consumeMType(final Buffer buffer) throws SipParseException {
        try {
            return consumeToken(buffer);
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Tried to consume m-type but buffer ended abruptly", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Tried to consume m-type but problem reading from underlying stream", e);
        }
    }

    /**
     * Consume a m-subtype, which according to RFC3261 section 25.1 Basic Rules
     * is:
     * 
     * <pre>
     * m-subtype        =  extension-token / iana-token
     * extension-token  =  ietf-token / x-token
     * ietf-token       =  token
     * x-token          =  "x-" token
     * iana-token       =  token
     * </pre>
     * 
     * And if you really read through all of that stuff it all boils down to
     * "token" so that is all we end up doing in this method. Hence, this method
     * is really only to put some context to consuming a media-type
     * 
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public static Buffer consumeMSubtype(final Buffer buffer) throws SipParseException {
        try {
            return consumeToken(buffer);
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Tried to consume m-type but buffer ended abruptly", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Tried to consume m-type but problem reading from underlying stream", e);
        }
    }

    /**
     * Helper method that counts the number of bytes that are considered part of
     * the next token in the {@link Buffer}.
     * 
     * @param buffer
     * @return a count of the number of bytes the next token contains or zero if
     *         no token is to be found within the buffer.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int getTokenCount(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        boolean done = false;
        int count = 0;
        buffer.markReaderIndex();
        while (buffer.hasReadableBytes() && !done) {
            final byte b = buffer.readByte();
            final boolean ok = isAlphaNum(b) || (b == DASH) || (b == PERIOD) || (b == EXCLAMATIONPOINT)
                    || (b == PERCENT) || (b == STAR) || (b == UNDERSCORE) || (b == PLUS) || (b == BACKTICK)
                    || (b == TICK) || (b == TILDE);
            if (ok) {
                ++count;
            } else {
                done = true;
            }
        }
        buffer.resetReaderIndex();
        return count;
    }

    /**
     * Check whether next byte is a alpha numeric one.
     * 
     * @param buffer
     * @return true if the next byte is a alpha numeric character, otherwise
     *         false
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static boolean isNextAlphaNum(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        if (buffer.hasReadableBytes()) {
            final byte b = buffer.peekByte();
            return isAlphaNum(b);
        }

        return false;
    }

    /**
     * Helper method for checking whether the supplied byte is a alphanumeric
     * character or not.
     * 
     * @param b
     * @return true if the byte is indeed a alphanumeric character, false
     *         otherwise
     */
    public static boolean isAlphaNum(final char ch) {
        return ((ch >= 97) && (ch <= 122)) || ((ch >= 48) && (ch <= 57)) || ((ch >= 65) && (ch <= 90));
    }

    public static boolean isAlphaNum(final byte b) {
        return isAlphaNum((char) b);
    }


    /**
     * Consume linear whitespace (LWS), which according to RFC3261 section 25.1
     * Basic Rules is:
     * 
     * LWS = [*WSP CRLF] 1*WSP ; linear whitespace
     * 
     * @param buffer
     * @return the number of bytes consumed
     */
    public static int consumeLWS(final Buffer buffer) throws SipParseException {
        final int i = buffer.getReaderIndex();
        consumeWS(buffer);

        // if we consume a CRLF we expect at least ONE WS to be present next
        if (consumeCRLF(buffer) > 0) {
            expectWS(buffer);
        }
        consumeWS(buffer);
        if (buffer.getReaderIndex() == i) {
            throw new SipParseException(i, "Expected at least 1 WSP");
        }
        return buffer.getReaderIndex() - i;
    }

    /**
     * Consume CR + LF
     * 
     * @param buffer
     * @return true if we indeed did consume CRLF, false otherwise
     */
    public static int consumeCRLF(final Buffer buffer) throws SipParseException {
        try {
            buffer.markReaderIndex();
            final byte cr = buffer.readByte();
            final byte lf = buffer.readByte();
            if ((cr == CR) && (lf == LF)) {
                return 2;
            }
        } catch (final IndexOutOfBoundsException e) {
            // fall through
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
        buffer.resetReaderIndex();
        return 0;
    }

    /**
     * Check so that the next byte in the passed in buffer is the expected one.
     * 
     * @param buffer the buffer that we will check.
     * @param expected the buffer that contains the expected byte (note, it is 1
     *            byte, not bytes)
     * @throws ParseException in case the expected byte is not the next byte in
     *             the buffer
     */
    public static void expect(final Buffer buffer, final byte expected) throws SipParseException, IOException {
        final byte actual = buffer.readByte();
        if (actual != expected) {
            final String actualStr = new String(new byte[] { actual });
            final String expectedStr = new String(new byte[] { expected });
            throw new SipParseException(buffer.getReaderIndex(), "Expected '" + expected + "' (" + expectedStr
                    + ") got '" + actual + "' (" + actualStr + ")");
        }
    }

    /**
     * Consume all the whitespace we find (WS)
     * 
     * @param buffer
     * @return true if we did consume something, false otherwise
     */
    public static int consumeWS(final Buffer buffer) throws SipParseException {
        try {
            boolean done = false;
            int count = 0;
            while (buffer.hasReadableBytes() && !done) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    buffer.readByte();
                    ++count;
                } else {
                    done = true;
                }
            }
            return count;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Check so that the next byte in the passed in buffer is the expected one.
     * 
     * @param buffer the buffer that we will check.
     * @param expected the expected char
     * @throws SipParseException in case the expected char is not the next char
     *             in the buffer or if there is an error reading from the
     *             underlying stream
     */
    public static void expect(final Buffer buffer, final char ch) throws SipParseException {
        try {
            final int i = buffer.readUnsignedByte();
            if (i != ch) {
                throw new SipParseException(buffer.getReaderIndex(), "Expected '" + ch + "' got '" + i + "'");
            }
        }
        catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }


    /**
     * 
     * @param buffer
     * @return an array where the first element is the name of the buffer and
     *         the second element is the value of the buffer
     * @throws SipParseException
     */
    public static SipHeader nextHeader(final Buffer buffer) throws SipParseException {
        try {

            final int startIndex = buffer.getReaderIndex();
            int nameIndex = 0;
            while (buffer.hasReadableBytes() && (nameIndex == 0)) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB) || isNext(buffer, COLON)) {
                    nameIndex = buffer.getReaderIndex();
                } else {
                    buffer.readByte();
                }
            }

            // Bad header! No HCOLON found! (or beginning thereof anyway)
            if (nameIndex == 0) {
                // probably ran out of bytes to read so lets just return null
                return null;
                // throw new SipParseException(buffer.getReaderIndex(),
                // "Expected HCOLON");
            }

            // final String name = buffer.copy(startIndex, nameIndex -
            // startIndex).toString(UTF8);
            final Buffer name = buffer.slice(startIndex, nameIndex);

            expectHCOLON(buffer);

            // Note, we are framing headers from a sip request or response which
            // is why we safely can assume that there will ALWAYS be a CRLF
            // after each line
            Buffer valueBuffer = buffer.readLine();

            // the header may be a folded one so check that and if so, consume
            // it too
            List<Buffer> foldedLines = null;
            boolean done = false;
            while (!done) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    consumeWS(buffer);
                    if (foldedLines == null) {
                        foldedLines = new ArrayList<Buffer>(2);
                    }
                    foldedLines.add(buffer.readLine());
                } else {
                    done = true;
                }
            }

            if (foldedLines != null) {
                // even though stupid, folded lines are not that common
                // so optimize if this ever becomes a problem.
                String stupid = valueBuffer.toString();
                for (final Buffer line : foldedLines) {
                    stupid += " " + line.toString();
                }
                valueBuffer = Buffers.wrap(stupid.getBytes());
            }

            return new SipHeaderImpl(name, valueBuffer);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

}
