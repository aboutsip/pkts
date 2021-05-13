package io.pkts.packet;

public interface IPv6Packet extends IPPacket {
    byte VERSION_IDENTIFIER = 6;
    int EXTENSION_HOP_BY_HOP = 0;
    int EXTENSION_DESTINATION_OPTIONS = 60;
    int EXTENSION_ROUTING = 43;
    int EXTENSION_FRAGMENT = 44;
    int EXTENSION_AH = 51;
    int EXTENSION_ESP = 50;
    int EXTENSION_MOBILITY = 135;
    short getTrafficClass();
    int getFlowLabel();
    int getHopLimit();
}
