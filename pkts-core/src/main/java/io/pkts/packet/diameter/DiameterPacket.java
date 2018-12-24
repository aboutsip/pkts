package io.pkts.packet.diameter;

import io.pkts.diameter.DiameterHeader;
import io.pkts.diameter.avp.FramedAvp;
import io.pkts.packet.impl.ApplicationPacket;

import java.util.List;

public interface DiameterPacket extends ApplicationPacket {

    DiameterHeader getHeader();

    List<FramedAvp> getAllAvps();
}
