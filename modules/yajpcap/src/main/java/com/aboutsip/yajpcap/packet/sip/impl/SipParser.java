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

    public static final byte PLUS = '+';

    public static final byte BACKTICK = '`';

    public static final byte TICK = '\'';

    public static final byte TILDE = '~';

    public static final byte EQ = '=';

    public static final byte SLASH = '/';

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
        if (consumeEQUAL(buffer)) {
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
            buffer.markReaderIndex();
            final Byte actual = buffer.readByte();
            buffer.resetReaderIndex();
            return actual == b;
        }

        return false;
    }

    /**
     * Find the index of the specified byte.
     * 
     * @param buffer
     *            the buffer
     * @param b
     *            the byte that we are looking for
     * @return the index of the byte or -1 (negative one) if it isn't found.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int indexOf(final Buffer buffer, final byte b) throws IndexOutOfBoundsException, IOException {
        buffer.markReaderIndex();
        int index = -1;
        boolean found = false;
        while (buffer.hasReadableBytes() && !found) {
            ++index;
            if (buffer.readByte() == b) {
                found = true;
            }
        }
        buffer.resetReaderIndex();
        if (found) {
            return index;
        } else {
            return -1;
        }
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
            buffer.markReaderIndex();
            final char next = (char)buffer.readByte();
            buffer.resetReaderIndex();
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
    public static void expectHCOLON(final Buffer buffer) throws SipParseException {
        try {
            consumeWS(buffer);
            expect(buffer, COLON);
            consumeSWS(buffer);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Expect the next byte to be a white space
     * 
     * @param buffer
     */
    public static void expectWS(final Buffer buffer) throws SipParseException {
        try {
            if (buffer.hasReadableBytes()) {

                final byte b = buffer.getByte(buffer.getReaderIndex());
                if ((b == SP) || (b == HTAB)) {
                    // ok, it was a WS so consume the byte
                    buffer.readByte();
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
    public static boolean consumeSWS(final Buffer buffer) {
        try {
            return consumeLWS(buffer);
        } catch (final SipParseException e) {
            // ignore since currently the consumeLWS will ONLY
            // throw this exception when it expected a WS at a
            // particular place and since SWS is all optional
            // we will silently just consume it, but return
            // false however
            return false;
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
    public static boolean consumeSTAR(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, STAR);
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
    public static boolean consumeSLASH(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, SLASH);
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
    public static boolean consumeEQUAL(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
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
    public static boolean consumeLPAREN(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
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
    public static boolean consumeRPAREN(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
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
    public static boolean consumeRAQUOT(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
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
    public static boolean consumeLAQUOT(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
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
    public static boolean consumeCOMMA(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
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
    public static boolean consumeSEMI(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
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
    public static boolean consumeCOLON(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
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
    public static boolean consumeLDQUOT(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        consumeSWS(buffer);
        if (isNext(buffer, DQUOT)) {
            buffer.readByte();
        } else {
            return false;
        }
        return true;
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
    public static boolean consumeRDQUOT(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        consumeSWS(buffer);
        if (isNext(buffer, DQUOT)) {
            buffer.readByte();
        } else {
            return false;
        }
        return true;
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
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    private static boolean consumeSeparator(final Buffer buffer, final byte b) throws IndexOutOfBoundsException,
    IOException {
        consumeSWS(buffer);
        if (isNext(buffer, b)) {
            buffer.readByte();
        } else {
            return false;
        }
        consumeSWS(buffer);
        return true;
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
        if (count == 0) {
            return null;
        }
        return buffer.readBytes(count);
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
            buffer.markReaderIndex();
            final byte b = buffer.readByte();
            buffer.resetReaderIndex();
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
     * @return true if we indeed did consume some LWS
     */
    public static boolean consumeLWS(final Buffer buffer) throws SipParseException {
        final int i = buffer.getReaderIndex();
        consumeWS(buffer);

        // if we consume a CRLF we expect at least ONE WS to be present next
        if (consumeCRLF(buffer)) {
            expectWS(buffer);
        }
        consumeWS(buffer);
        if (buffer.getReaderIndex() == i) {
            throw new SipParseException(i, "Expected at least 1 WSP");
        }
        return true;
    }

    /**
     * Consume CR + LF
     * 
     * @param buffer
     * @return true if we indeed did consume CRLF, false otherwise
     */
    public static boolean consumeCRLF(final Buffer buffer) throws SipParseException {
        try {
            buffer.markReaderIndex();
            final byte cr = buffer.readByte();
            final byte lf = buffer.readByte();
            if ((cr == CR) && (lf == LF)) {
                return true;
            }
        } catch (final IndexOutOfBoundsException e) {
            // fall through
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
        buffer.resetReaderIndex();
        return false;
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
    public static boolean consumeWS(final Buffer buffer) throws SipParseException {
        try {
            boolean consumed = false;
            boolean done = false;
            while (buffer.hasReadableBytes() && !done) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    buffer.readByte();
                    consumed = true;
                } else {
                    done = true;
                }
            }
            return consumed;
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
