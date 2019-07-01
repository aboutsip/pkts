package io.pkts.tgpp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.pkts.tgpp.ReferencePoint.N26;
import static io.pkts.tgpp.ReferencePoint.S10;
import static io.pkts.tgpp.ReferencePoint.S11;
import static io.pkts.tgpp.ReferencePoint.S2a;
import static io.pkts.tgpp.ReferencePoint.S2b;
import static io.pkts.tgpp.ReferencePoint.S3;
import static io.pkts.tgpp.ReferencePoint.S5;
import static io.pkts.tgpp.ReferencePoint.S8;
import static io.pkts.tgpp.ReferencePoint.S4;
import static io.pkts.tgpp.ReferencePoint.S16;

/**
 * This file has been auto generated. Do not manually edit.
 *
 * @author jonas@jonasborjesson.com
 */
public enum Gtp2InformationElements {

    Reserved(0, false, false, "29.274", ""),
    Echo_Request(1, true, false, "29.274", ""),
    Echo_Response(2, false, true, "29.274", ""),
    Version_Not_Supported_Indication(3, false, true, "29.274", ""),
    Create_Session_Request(32, true, false, "29.274", ""),
    Create_Session_Response(33, false, true, "29.274", ""),
    Delete_Session_Request(36, true, false, "29.274", ""),
    Delete_Session_Response(37, false, true, "29.274", ""),
    Modify_Bearer_Request(34, true, false, "29.274", ""),
    Modify_Bearer_Response(35, false, true, "29.274", ""),
    Remote_UE_Report_Notification(40, true, false, "29.274", ""),
    Remote_UE_Report_Acknowledge(41, false, true, "29.274", ""),
    Change_Notification_Request(38, true, false, "29.274", ""),
    Change_Notification_Response(39, false, true, "29.274", ""),
    Resume_Notification(164, true, false, "29.274", ""),
    Resume_Acknowledge(165, false, true, "29.274", ""),
    Modify_Bearer_Command(64, true, false, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    Modify_Bearer_Failure_Indication(65, false, true, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    Delete_Bearer_Command(66, true, false, "29.274", "" ,S11,S4,S5,S8),
    Delete_Bearer_Failure_Indication(67, false, true, "29.274", "" ,S11,S4,S5,S8),
    Bearer_Resource_Command(68, true, false, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    Bearer_Resource_Failure_Indication(69, false, true, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    Downlink_Data_Notification_Failure_Indication(70, true, false, "29.274", "" ,S11,S4),
    Trace_Session_Activation(71, true, false, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    Trace_Session_Deactivation(72, true, false, "29.274", "" ,S11,S4),
    Stop_Paging_Indication(73, true, false, "29.274", "" ,S11,S4),
    Create_Bearer_Request(95, true, true, "29.274", ""),
    Create_Bearer_Response(96, false, true, "29.274", ""),
    Update_Bearer_Request(97, true, true, "29.274", ""),
    Update_Bearer_Response(98, false, true, "29.274", ""),
    Delete_Bearer_Request(99, true, true, "29.274", ""),
    Delete_Bearer_Response(100, false, true, "29.274", ""),
    Delete_PDN_Connection_Set_Request(101, true, false, "29.274", ""),
    Delete_PDN_Connection_Set_Response(102, false, true, "29.274", ""),
    PGW_Downlink_Triggering_Notification(103, true, false, "29.274", ""),
    PGW_Downlink_Triggering_Acknowledge(104, false, true, "29.274", ""),
    Identification_Request(128, true, false, "29.274", "" ,S3,S10,S16,N26),
    Identification_Response(129, false, true, "29.274", "" ,S3,S10,S16,N26),
    Context_Request(130, true, false, "29.274", "" ,S3,S10,S16,N26),
    Context_Response(131, false, true, "29.274", "" ,S3,S10,S16,N26),
    Context_Acknowledge(132, false, true, "29.274", "" ,S3,S10,S16,N26),
    Forward_Relocation_Request(133, true, false, "29.274", "" ,S3,S10,S16,N26),
    Forward_Relocation_Response(134, false, true, "29.274", "" ,S3,S10,S16,N26),
    Forward_Relocation_Complete_Notification(135, true, false, "29.274", "" ,S3,S10,S16,N26),
    Forward_Relocation_Complete_Acknowledge(136, false, true, "29.274", "" ,S3,S10,S16,N26),
    Forward_Access_Context_Notification(137, true, false, "29.274", "" ,S10,S16),
    Forward_Access_Context_Acknowledge(138, false, true, "29.274", "" ,S10,S16),
    Relocation_Cancel_Request(139, true, false, "29.274", "" ,S3,S10,S16,N26),
    Relocation_Cancel_Response(140, false, true, "29.274", "" ,S3,S10,S16,N26),
    Configuration_Transfer_Tunnel(141, true, false, "29.274", "" ,S10,N26),
    RAN_Information_Relay(152, true, false, "29.274", "" ,S3,S16),
    Detach_Notification(149, true, false, "29.274", ""),
    Detach_Acknowledge(150, false, true, "29.274", ""),
    CS_Paging_Indication(151, true, false, "29.274", ""),
    Alert_MME_Notification(153, true, false, "29.274", ""),
    Alert_MME_Acknowledge(154, false, true, "29.274", ""),
    UE_Activity_Notification(155, true, false, "29.274", ""),
    UE_Activity_Acknowledge(156, false, true, "29.274", ""),
    ISR_Status_Indication(157, true, false, "29.274", ""),
    UE_Registration_Query_Request(158, true, false, "29.274", ""),
    UE_Registration_Query_Response(159, false, true, "29.274", ""),
    Suspend_Notification(162, true, false, "29.274", ""),
    Suspend_Acknowledge(163, false, true, "29.274", ""),
    Create_Forwarding_Tunnel_Request(160, true, false, "29.274", ""),
    Create_Forwarding_Tunnel_Response(161, false, true, "29.274", ""),
    Create_Indirect_Data_Forwarding_Tunnel_Request(166, true, false, "29.274", ""),
    Create_Indirect_Data_Forwarding_Tunnel_Response(167, false, true, "29.274", ""),
    Delete_Indirect_Data_Forwarding_Tunnel_Request(168, true, false, "29.274", ""),
    Delete_Indirect_Data_Forwarding_Tunnel_Response(169, false, true, "29.274", ""),
    Release_Access_Bearers_Request(170, true, false, "29.274", ""),
    Release_Access_Bearers_Response(171, false, true, "29.274", ""),
    Downlink_Data_Notification(176, true, false, "29.274", ""),
    Downlink_Data_Notification_Acknowledge(177, false, true, "29.274", ""),
    PGW_Restart_Notification(179, true, false, "29.274", ""),
    PGW_Restart_Notification_Acknowledge(180, false, true, "29.274", ""),
    Update_PDN_Connection_Set_Request(200, true, false, "29.274", ""),
    Update_PDN_Connection_Set_Response(201, false, true, "29.274", ""),
    Modify_Access_Bearers_Request(211, true, false, "29.274", ""),
    Modify_Access_Bearers_Response(212, false, true, "29.274", ""),
    MBMS_Session_Start_Request(231, true, false, "29.274", ""),
    MBMS_Session_Start_Response(232, false, true, "29.274", ""),
    MBMS_Session_Update_Request(233, true, false, "29.274", ""),
    MBMS_Session_Update_Response(234, false, true, "29.274", ""),
    MBMS_Session_Stop_Request(235, true, false, "29.274", ""),
    MBMS_Session_Stop_Response(236, false, true, "29.274", "");

    private static Map<Integer, Gtp2InformationElements> byType = new HashMap<>();

    static {
        Arrays.stream(Gtp2InformationElements.values()).forEach(e -> byType.put(e.getType(), e));
    }

    public static Gtp2InformationElements lookup(final int type) {
        return byType.get(type);
    }

    private final int type;
    private final boolean isInitial;
    private final boolean isTriggered;

    Gtp2InformationElements(final int type, final boolean isInitial, final boolean isTriggered, final String specification, final String section, final ReferencePoint ... refs) {
        this.type = type;
        this.isInitial = isInitial;
        this.isTriggered = isTriggered;
    }

    public int getType() {
        return type;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isTriggered() {
        return isTriggered;
    }

}