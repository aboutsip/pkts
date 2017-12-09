# Pkts.io Changelog

## Release 3.0.2
* Major restructuring of the packet hierarchy, which also has introduced API breaking changes. 
* Previously, any packet would inherit from their parent packet, creating a strongly coupled relationship between packets. Although unlikely that a UDP packet would be carried over anything but an IP packet, treating a UDP packet as decoupled from this assumption makes sense. However, that means that methods, such source and destination IP no longer are part of the UDP packet, since they are after all an IP packet construct. Hence

## Release 3.0.0

Major release with breaking API changes compared to the 2.x branch. All SIP messages are now immutable.

### New Features

* All SIP Messages are now immutable. This includes headers and URIs as well.
* New fluent builder pattern for constructing SIP messages.
* See siplib.io for tutorials and examples.
* SIP Message Equality - quite simplified version though.
* SIP request and response line equality (`SipInitialLine.equals`) 

### Bug Fixes

* Bugs in IPV4 and SLL packet framing - Fixes a bug in the IPV4 packet framer where it would wrongly advance the read pointer in the header buffer while parsing the frame, which would lead to errors if you would ever try to write the packet back out.
* Bug in SLL packet framing where it had the packet type constant wrong.
* Fixe of issue #27 - io.pkts.packet.sip.SipParseException appeared twice in two different packages. Renamed the one in `pkts-core` to `SipPacketParseException`.
* Transport.isTLS() was wrong. It compared itself against "tcp" and not "tls"
* Removed the assumption that IP packets are always transported over Ethernet.
* Support for "LINKTYPE_RAW" - i.e. straight IP, without Ethernet

### API Breaking Changes

One reason the major version was bumped is due to the API breaking changes that have been introduced. The main change is that all SIP messages are now immutable and as such, all `setXXX` has been removed. If you want to change a message you have to copy it, which will give you a builder object, change what you need to change and then build it again. See [siplib.io](http://siplib.io) for examples.

A smaller change was to rename `io.pkts.packet.sip.SipParseException` in `pkts-core` to `SipPacketParseException` instead. This was because there already was a `SipParseException` as part of the `pkts-sip` packet. So, if you were using the `SipParseException` from the `pkts-core` module then you need to update your code to use `SipPacketParseException`.


