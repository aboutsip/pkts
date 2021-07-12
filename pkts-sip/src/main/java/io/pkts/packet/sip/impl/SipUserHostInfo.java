package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;

import java.io.IOException;

import static io.pkts.packet.sip.impl.SipParser.*;

/**
 * Represents the user/host info portion of the SIP URI.
 */
public class SipUserHostInfo {
    private final Buffer user;
    private final Buffer host;
    private final Buffer port;

    /**
     * Frames a SIP URI user/host info portion, specified in RFC 3261 as:
     *
     * <pre>
     * SIP-URI  =  "sip:" [ userinfo ] hostport
     *             uri-parameters [ headers ]
     * SIPS-URI =  "sips:" [ userinfo ] hostport
     *             uri-parameters [ headers ]
     *
     * userinfo         =  ( user / telephone-subscriber ) [ ":" password ] "@"
     * user             =  1*( unreserved / escaped / user-unreserved )
     * user-unreserved  =  "&" / "=" / "+" / "$" / "," / ";" / "?" / "/"
     * password         =  *( unreserved / escaped / "&" / "=" / "+" / "$" / "," )
     * hostport         =  host [ ":" port ]
     * </pre>
     *
     * The URI is consumed up to the start uri-parameters or headers section, whichever
     * is encountered first.
     *
     * @param buffer a buffer, which should begin at the start of the user/host info section
     * @return a SipUserHostInfo object, encoding the parsed information
     * @throws SipParseException
     * @throws IOException
     */
    public static SipUserHostInfo frame(final Buffer buffer) throws SipParseException, IOException {
        final Parser parser = new Parser(buffer);
        return parser.parse();
    }

    private SipUserHostInfo(final Buffer user, final Buffer host, final Buffer port) {
        this.user = user;
        this.host = host;
        this.port = port;
    }

    /**
     * Accesses the user portion of the SIP URI
     *
     * @return a buffer containing the user portion, or null if not specified in the URI
     */
    public Buffer getUser() {
        return user;
    }

    /**
     * Accesses the host portion of the SIP URI
     *
     * To be valid, the URI must contain the host portion--this method should always return a value.
     *
     * @return a buffer containing the host portion
     */
    public Buffer getHost() {
        return host;
    }

    /**
     * Accesses the port portion of the SIP URI
     *
     * @return a buffer containing the port portion, or null if not specified in the URI
     */
    public Buffer getPort() {
        return port;
    }

    /**
     * Parser
     *
     * Parsing the User/Host info requires complex state tracking, as we must explore several
     * avenues simultaneously until we can rule one or more out (for example, until we encounter
     * an '@' symbol, we may be parsing a user portion or a host portion).
     */
    private static class Parser {
        private final Buffer buffer;
        private final int bufferStartIndex;
        private Buffer user;
        private Buffer host;
        private Buffer port;

        // Host/Port parser
        private enum HostPortParseState {
            HOST, PORT, INVALID, END, OUT_OF_CHARS
        }
        private HostPortParseState parseState;
        private int stateStartIndex;
        private int stateCount;
        private int hostPortEndIndex; // Index where the host/port portion ends
        private int errorIndex; // Index of character that caused error, or -1 if no specific character

        // Host parser
        private enum HostType {
            HOSTNAME_OR_IPV4, IPV6
        }
        private HostType hostType;
        private boolean isIPv6BracketsClosed;

        // Hostname parser
        private enum HostnameParseState {
            BEGIN, INVALID, IN_LABEL, IN_LABEL_DASH, AT_SEPARATOR
        }
        private HostnameParseState hostnameParseState;
        private boolean hostnameValidTLD;

        // IPv4 parser
        private enum IPv4ParseState {
            BEGIN, INVALID, ACCUMULATE_DIGITS, AT_SEPARATOR
        }
        private IPv4ParseState ipv4ParseState;
        private int ipv4NumDigits;
        private int ipv4NumSegments;

        // IPv6 parser
        private static class IPv6Parser {

            public enum State {
                INVALID, HEX_PART, HEX_SEQ, IPV4
            }

            public State state = State.HEX_PART;
            public int numDigits = 0;
            public int numHexDigits = 0;
            public int numDoubleColons = 0;
            public int numColons = 0;
            public int numPeriods = 0;

        }
        private IPv6Parser ipv6Parser;

        private Parser(final Buffer buffer) {
            this.buffer = buffer;
            this.bufferStartIndex = buffer.getReaderIndex();
        }

        /**
         * Parses and consumes the user info, host, and port of SIP URI
         *
         * @throws SipParseException
         * @throws IOException
         */
        private SipUserHostInfo parse() throws SipParseException, IOException {

            // first, try and determine if there is a user portion
            // present. If we can't find one within MAX_BYTES then assume
            // there isn't one there. Note, the way this method is typically used
            // is that you have already framed a buffer so it is very unlikely that
            // a header will be this long so we will most often quite much
            // earlier due to buffer.hasReadableBytes() return false
            resetHostPortParser();

            while (user == null && buffer.hasReadableBytes() && stateCount < SipParser.MAX_LOOK_AHEAD) {
                final byte b = buffer.readByte();

                if (b == SipParser.AT) {
                    final int userCount = buffer.getReaderIndex() - bufferStartIndex;

                    buffer.setReaderIndex(bufferStartIndex);

                    if (userCount - 1 == 0) {
                        throw new SipParseException(userCount - 1,
                                "No user portion in URI despite the presence of a '@'");
                    }
                    user = buffer.readBytes(userCount - 1);
                    buffer.readByte(); // consume the '@' sign

                    // Re-start parsing the remainder of the URI
                    resetHostPortParser();
                } else {
                    // Keep parsing
                    processHostPortCharacter(b);
                }
            }

            // Scan the rest of the URI
            // stop if (1) an error makes the URI invalid, (2) we run out of chars, or (3) we exceed lookahead
            while (parseState != HostPortParseState.INVALID && parseState != HostPortParseState.END &&
                    buffer.hasReadableBytes() && stateCount < MAX_LOOK_AHEAD) {
                final byte b = buffer.readByte();
                processHostPortCharacter(b);
            }

            if (parseState != HostPortParseState.END && parseState != HostPortParseState.INVALID && buffer.hasReadableBytes()) {
                final int charsRead = buffer.getReaderIndex() - bufferStartIndex;
                throw new SipParseException(charsRead, "Was never able to find the end of the SIP URI. Gave up after " +
                        "%d characters");
            } else {
                // Out of characters
                processOutOfCharacters();

                if (parseState == HostPortParseState.INVALID) {
                    // Formatting error encountered along the way
                    final int charsReadBeforeError = errorIndex - bufferStartIndex;
                    throw new SipParseException(charsReadBeforeError,
                            "The SIP URI does not specify a valid host. Error encountered after %d characters.");
                } else {
                    // Leave the buffer at the end of the host
                    buffer.setReaderIndex(hostPortEndIndex);
                }
            }

            // Successful parsing, return a SIPUserHostInfo object
            return new SipUserHostInfo(user, host, port);
        }

        /**
         * Resets the Host/Port parser's internal state
         */
        private void resetHostPortParser() {
            parseState = HostPortParseState.HOST;
            hostType = HostType.HOSTNAME_OR_IPV4;
            isIPv6BracketsClosed = false;
            hostnameParseState = HostnameParseState.BEGIN;
            ipv4ParseState = IPv4ParseState.BEGIN;
            ipv4NumSegments = 0;
            ipv6Parser = new IPv6Parser();
            stateStartIndex = buffer.getReaderIndex();
            stateCount = 0;
            host = null;
            port = null;
            hostPortEndIndex = 0;
            errorIndex = -1;
        }

        /**
         * Transitions the Host/Port parser to the specified state
         *
         * The characters collected in the previous state are filed in the appropriate
         * instance variable.
         *
         * @param newState the state to transition to
         * @throws IOException
         */
        private void enterHostPortParseState(final HostPortParseState newState) throws IOException {
            final HostPortParseState oldState = parseState;

            final int separatorChars;
            if (newState == HostPortParseState.OUT_OF_CHARS || newState == HostPortParseState.INVALID) {
                separatorChars = 0;
            } else {
                separatorChars = 1;
            }

            // Grab the old buffer and store it
            // expects we are one character ahead of stop index
            buffer.setReaderIndex(stateStartIndex);
            final Buffer res = buffer.readBytes(stateCount);
            switch (oldState) {
            case HOST:
                host = res;
                switch (hostType) {
                    case HOSTNAME_OR_IPV4:
                        host = res;
                        break;
                    case IPV6:
                        res.setReaderIndex(1); // skips "["
                        host = res.readBytes(stateCount - 2); // without "[" and "]"
                        break;
                }
                hostPortEndIndex = buffer.getReaderIndex();
                break;
            case PORT:
                port = res;
                hostPortEndIndex = buffer.getReaderIndex();
                break;
            }

            // Consume the separators
            buffer.readBytes(separatorChars);

            if (newState == HostPortParseState.INVALID && errorIndex < 0) {
                errorIndex = stateStartIndex;
            } else if (newState != HostPortParseState.INVALID) {
                errorIndex = -1;
            }

            // Set the new state and start index
            parseState = newState;
            stateStartIndex += stateCount + separatorChars;
            stateCount = -1; // will be incremented at the end of processHostPortCharacter
        }

        /**
         * Advances the host/port parser using the given character
         *
         * @param b the character to parse
         * @throws IOException
         */
        private void processHostPortCharacter(final byte b) throws IOException {
            switch (parseState) {
            case HOST:
                switch (hostType) {
                    case HOSTNAME_OR_IPV4:
                        if (b == LSBRACKET && stateCount == 0) {
                            // start IPv6
                            hostType = HostType.IPV6;
                            isIPv6BracketsClosed = false;
                        } else if (b == COLON) {
                            if (hostPortIsValid()) {
                                enterHostPortParseState(HostPortParseState.PORT);
                            } else {
                                enterHostPortParseState(HostPortParseState.INVALID);
                            }
                        } else if (b == SEMI || b == QUESTIONMARK) {
                            if (hostPortIsValid()) {
                                enterHostPortParseState(HostPortParseState.END);
                            } else {
                                enterHostPortParseState(HostPortParseState.INVALID);
                            }
                        } else {
                            // Check if the character is valid for the host portion
                            // Host portion could be one of three things, check all three
                            processHostnameCharacter(b);
                            processIPv4Character(b);
                        }
                        break;
                    case IPV6:
                        if (isIPv6BracketsClosed) {
                            if (b == COLON) {
                                if (hostPortIsValid()) {
                                    enterHostPortParseState(HostPortParseState.PORT);
                                } else {
                                    enterHostPortParseState(HostPortParseState.INVALID);
                                }
                            } else if (b == SEMI || b == QUESTIONMARK) {
                                if (hostPortIsValid()) {
                                    enterHostPortParseState(HostPortParseState.END);
                                } else {
                                    enterHostPortParseState(HostPortParseState.INVALID);
                                }
                            } else {
                                enterHostPortParseState(HostPortParseState.INVALID);
                            }
                        } else if (b == RSBRACKET) {
                            isIPv6BracketsClosed = true;
                            if (ipv6Parser.state != IPv6Parser.State.INVALID && !hostIsValidIPv6()) {
                                errorIndex = buffer.getReaderIndex() - 1;
                                ipv6Parser.state = IPv6Parser.State.INVALID;
                                enterHostPortParseState(HostPortParseState.INVALID);
                            }
                        } else {
                            processIPv6Character(b);
                        }
                        break;
                }
                break;
            case PORT:
                if (b == SEMI || b == QUESTIONMARK) {
                    if (hostPortIsValid()) {
                        enterHostPortParseState(HostPortParseState.END);
                    } else {
                        enterHostPortParseState(HostPortParseState.INVALID);
                    }
                } else if (!isDigit(b)) {
                    errorIndex = buffer.getReaderIndex() - 1;
                    enterHostPortParseState(HostPortParseState.INVALID);
                }
                break;
            }

            stateCount++;
        }

        private boolean hostPortIsValid() {
            switch (parseState) {
            case HOST:
                return hostIsValidHostname() || hostIsValidIPv4() || hostIsValidIPv6();
            case PORT:
                return stateCount > 0;
            case END:
            case OUT_OF_CHARS:
                return true;
            default:
                return false;
            }
        }

        private void processOutOfCharacters() throws IOException {
            if (hostPortIsValid()) {
                enterHostPortParseState(HostPortParseState.OUT_OF_CHARS);
            } else {
                enterHostPortParseState(HostPortParseState.INVALID);
            }
        }

        /**
         * Advances the hostname parser using the given character
         *
         * @param b the character to parse
         * @throws IOException
         */
        private void processHostnameCharacter(final byte b) throws IOException {
            switch (hostnameParseState) {
            case AT_SEPARATOR:
                // Fall through
            case BEGIN:
                // First character needs to start a domain label
                if (isAlpha(b)) {
                    hostnameParseState = HostnameParseState.IN_LABEL;
                    hostnameValidTLD = true;
                } else if (isDigit(b)) {
                    hostnameParseState = HostnameParseState.IN_LABEL;
                    hostnameValidTLD = false;
                    errorIndex = buffer.getReaderIndex() - 1;
                } else {
                    errorIndex = buffer.getReaderIndex() - 1;
                    hostnameParseState = HostnameParseState.INVALID;
                }
                break;
            case IN_LABEL:
                // In the midst of a label, expecting another alphanum character or a dash
                if (b == DASH) {
                    hostnameParseState = HostnameParseState.IN_LABEL_DASH;
                } else if (b == PERIOD) {
                    hostnameParseState = HostnameParseState.AT_SEPARATOR;
                } else if (!isAlphaNum(b)) {
                    errorIndex = buffer.getReaderIndex() - 1;
                    hostnameParseState = HostnameParseState.INVALID;
                }
                break;
            case IN_LABEL_DASH:
                // Last character was a dash, next must be alphanum OR another dash
                if (b == DASH) {
                    break;
                } else if (isAlphaNum(b)) {
                    hostnameParseState = HostnameParseState.IN_LABEL;
                } else {
                    errorIndex = buffer.getReaderIndex() - 1;
                    hostnameParseState = HostnameParseState.INVALID;
                }
                break;
            }
        }

        /**
         * Determines if the hostname parser is currently in a valid state
         *
         * To be valid, the last character must be part of a valid top-level domain
         * label, or at a period directly following the label.
         *
         * @return true if valid, false otherwise
         */
        private boolean hostIsValidHostname() {
            // Per RFC 3261:
            // hostname         =  *( domainlabel "." ) toplabel [ "." ]
            // thus, as uncommon as it may seem, a stray period at the end of the hostname is legal
            return (hostnameParseState == HostnameParseState.IN_LABEL ||
                    hostnameParseState == HostnameParseState.AT_SEPARATOR) && hostnameValidTLD;
        }

        /**
         * Advances the IPv4 host parser using the given character
         *
         * @param b the character to parse
         * @throws IOException
         */
        private void processIPv4Character(final byte b) throws IOException {
            switch (ipv4ParseState) {
            case AT_SEPARATOR:
                // Fall through
            case BEGIN:
                // First character must be a digit
                if (isDigit(b)) {
                    ipv4ParseState = IPv4ParseState.ACCUMULATE_DIGITS;
                    ipv4NumSegments++;
                    ipv4NumDigits = 1;
                } else {
                    errorIndex = buffer.getReaderIndex() - 1;
                    ipv4ParseState = IPv4ParseState.INVALID;
                }
                break;
            case ACCUMULATE_DIGITS:
                if (b == PERIOD) {
                    ipv4ParseState = IPv4ParseState.AT_SEPARATOR;
                } else if (!isDigit(b) || ++ipv4NumDigits > 3) {
                    errorIndex = buffer.getReaderIndex() - 1;
                    ipv4ParseState = IPv4ParseState.INVALID;
                }
                break;
            }
        }

        /**
         * Determines if the IPv4 host parser is currently in a valid state
         *
         * To be valid, the address must contain exactly 4 digit segments and not
         * have failed for any other reason.
         *
         * @return true if valid, false otherwise
         */
        private boolean hostIsValidIPv4() {
            return ipv4ParseState == IPv4ParseState.ACCUMULATE_DIGITS && ipv4NumSegments == 4;
        }

        /**
         * Advances the IPv6 host parser using the given character
         *
         * Per RFC 3261:
         * <pre>
         * IPv4address    =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
         * IPv6address    =  hexpart [ ":" IPv4address ]
         * hexpart        =  hexseq / hexseq "::" [ hexseq ] / "::" [ hexseq ]
         * hexseq         =  hex4 *( ":" hex4)
         * hex4           =  1*4HEXDIG
         * </pre>
         *
         * @param b the character to parse
         * @throws IOException
         */
        private void processIPv6Character(final byte b) throws IOException {
            char c = (char)b;
            switch (ipv6Parser.state) {
                case INVALID:
                    return;
                case HEX_PART:
                    if (isHexDigit(b) && ipv6Parser.numColons == 0) {
                        // count hex digit and number digits
                        ipv6Parser.numHexDigits++;
                        if (isDigit(b)) {
                            ipv6Parser.numDigits++;
                        }
                        ipv6Parser.state = IPv6Parser.State.HEX_SEQ;
                        return;
                    } else if (b == COLON) {
                        if (ipv6Parser.numColons == 0) {
                            ipv6Parser.numColons = 1;
                            return;
                        } else if (ipv6Parser.numColons == 1) {
                            ipv6Parser.numColons = 0;
                            ipv6Parser.numDoubleColons++;
                            ipv6Parser.state = IPv6Parser.State.HEX_SEQ;
                            return;
                        }
                    }
                    break;
                case HEX_SEQ:
                    if (isHexDigit(b)) {
                        // count hex digit and number digits
                        ipv6Parser.numHexDigits++;
                        if (isDigit(b)) {
                            ipv6Parser.numDigits++;
                        }
                        ipv6Parser.numColons = 0;
                        if (ipv6Parser.numHexDigits <= 4) {
                            // within 1-4 hex digits
                            return;
                        }
                    } else if (b == COLON && ipv6Parser.numHexDigits > 0) {
                        // start next hex or optional ipv4
                        ipv6Parser.numColons++;
                        ipv6Parser.numDigits = 0;
                        ipv6Parser.numHexDigits = 0;
                        return;
                    } else if (b == COLON && ipv6Parser.numColons == 0 && ipv6Parser.numDoubleColons == 1) {
                        // double colons from HEX_PART and start IPv4 right away
                        ipv6Parser.state = IPv6Parser.State.IPV4;
                        ipv6Parser.numPeriods = 0;
                        ipv6Parser.numDigits = 0;
                        ipv6Parser.numHexDigits = 0;
                        return;
                    } else if (b == COLON && ipv6Parser.numColons == 1 && ipv6Parser.numDoubleColons == 0) {
                        // start last hex seq
                        ipv6Parser.numColons = 0;
                        ipv6Parser.numDoubleColons++;
                        return;
                    } else if (b == PERIOD && ipv6Parser.numDigits > 0 && ipv6Parser.numDigits <= 3) {
                        // start optional ipv4
                        ipv6Parser.state = IPv6Parser.State.IPV4;
                        ipv6Parser.numPeriods = 1;
                        ipv6Parser.numDigits = 0;
                        ipv6Parser.numHexDigits = 0;
                        return;
                    }
                    break;
                case IPV4:
                    if (isDigit(b)) {
                        ipv6Parser.numDigits++;
                        if (ipv6Parser.numDigits <= 3) {
                            // within 1-3 digits for ipv4
                            return;
                        }
                    } else if (b == PERIOD && ipv6Parser.numDigits > 0 && ipv6Parser.numDigits <= 3 && ipv6Parser.numPeriods <= 3) {
                        ipv6Parser.state = IPv6Parser.State.IPV4;
                        ipv6Parser.numPeriods++;
                        ipv6Parser.numDigits = 0;
                        if (ipv6Parser.numPeriods <= 3) {
                            return;
                        }
                    }
                    break;
            }

            errorIndex = buffer.getReaderIndex() - 1;
            ipv6Parser.state = IPv6Parser.State.INVALID;
        }

        /**
         * Determines if the IPv6 host parser is currently in a valid state
         *
         * To be valid, the address must contain exactly 4 digit segments and not
         * have failed for any other reason.
         *
         * @return true if valid, false otherwise
         */
        private boolean hostIsValidIPv6() {
            switch (ipv6Parser.state) {
                case INVALID:
                case HEX_PART:
                    return false;
                case HEX_SEQ: return ipv6Parser.numHexDigits > 0 || ipv6Parser.numDoubleColons == 1;
                case IPV4: return ipv6Parser.numPeriods == 3 && ipv6Parser.numDigits > 0;
            }
            return false;
        }
    }
}
