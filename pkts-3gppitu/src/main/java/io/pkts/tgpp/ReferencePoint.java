package io.pkts.tgpp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This file has been auto generated. Do not manually edit.
 *
 * @author jonas@jonasborjesson.com
 */
public enum ReferencePoint {

    Bi(new String[]{"Diameter"}, new String[]{"Charging Collection Function (CCF) <> Billing System"}, "Charging Collection Function CDR interface for data records from IMS core elements.", new String[]{"32.225"}),
    Cr(new String[]{"SIP"}, new String[]{"MRFC <> AS"}, "Conveys media control requests, responses and notifications as well as documents between the MRFC and an Application Server", new String[]{"23.218"}),
    Cx(new String[]{"Diameter"}, new String[]{"I/S-CSCF <> HSS"}, "Provides subscriber data to the S-CSCF", new String[]{"29.228","29.229"}),
    Dw(new String[]{"Diameter"}, new String[]{"AAA Server <> SLF"}, "Enables AAA Server to identify correct HSS for a specific user", new String[]{"29.234"}),
    Dx(new String[]{"Diameter"}, new String[]{"I-CSCF <> SLF"}, "For IMS subscribers finds the correct HSS in a multi-HSS environment.", new String[]{"29.228","29.229"}),
    Dz(new String[]{"Diameter"}, new String[]{"BSF <> SLF"}, "The BSF obtains the identity of HSS containing the required subscriber specific data.", new String[]{"29.109","33.220"}),
    Gf(new String[]{"MAP"}, new String[]{"SGSN<>EIR"}, "Used to identify valid user equipment based on the device’s IMEI.", new String[]{""}),
    Gi(new String[]{"IP"}, new String[]{"GGSN <> PDN"}, "Interface between mobile network and external public or private IP networks (for example, the Internet or IMS network).", new String[]{"29.061","23.002"}),
    Dh(new String[]{"Diameter"}, new String[]{"AS <> SLF"}, "Enables AS to find the HSS holding the applicable User Profile information in a multi-HSS environment.", new String[]{"29.328","29.329"}),
    Gm(new String[]{"SIP"}, new String[]{"UE <> P-CSCF"}, "Supports exchange of messages between SIP user equipment (UE) or a VoIP Gateway and P-CSCF", new String[]{"23.002"}),
    Gmb(new String[]{"Diameter"}, new String[]{"GGSN <> BM-SC"}, "Supports exchange of signaling between the GGSN and the Broadcast-Multicast Service Center for MBMS users.", new String[]{"29.061","23.246","23.002"}),
    Gn(new String[]{"GTPv1"}, new String[]{"SGSN <> GGSN"}, "Conveys control plane messages for establishing and managing tunnels for providing packet data network access. The Gn also manages user plane data packet tunnels from the RAN to the GGSN.", new String[]{"29.060","23.002"}),
    Gp(new String[]{"GTPv1"}, new String[]{"SGSN <> GGSN in other PLMN"}, "Similar to Gn, connects SGSN in visited network to GGSN in home PLMN.", new String[]{"29.060","23.002"}),
    Gq(new String[]{"Diameter"}, new String[]{"AF <> PDF"}, "Supports dynamic QoS-related service information exchanged between the Policy Decision Function (PDF) and the Application Function (AF).", new String[]{"29.209"}),
    Gr(new String[]{"MAP"}, new String[]{"SGSN <> HLR"}, "Authentication, authorization and mobility management of UE within the mobile network.", new String[]{"29.002","23.002"}),
    Gx(new String[]{"Diameter"}, new String[]{"PCEF/PGW <> PCRF"}, "Provides QoS, policy and charging information to the Policy Enforcement Function that resides within the GGSN or PGW.", new String[]{"29.211","29.212","23.203"}),
    Gxa(new String[]{"Diameter"}, new String[]{"PCRF <> BBERF"}, "Transfer of (QoS) policy information from the PCRF to the trusted non-access", new String[]{"29.212"}),
    Gxb(new String[]{"Diameter"}, new String[]{"ePDG <> vPCRF"}, "Transfer of (QoS) policy information from the PCRF to trusted non-access.", new String[]{"23.203"}),
    Gxc(new String[]{"Diameter"}, new String[]{"PCRF <> BBERF"}, "Transfer of (QoS) policy information from the PCRF to the serving gateway.", new String[]{"23.203"}),
    Gxx(new String[]{"Diameter"}, new String[]{"PCRF<>BBERF"}, "Conveys (QoS) policy control from the PCRF.", new String[]{"23.203"}),
    Gy(new String[]{"Diameter"}, new String[]{"PCEF/PGW and OCS"}, "Provides online charging control for event and session based service charging.", new String[]{"32.240","32.299","32.251","23.203:","RFC","4006"}),
    Gz(new String[]{"Diameter"}, new String[]{"PCEF/PGW <> OFCS"}, "Provides transport of service data flow based information for offline charging.", new String[]{"32.240","","32.295","23.203"}),
    Iq(new String[]{"H.248"}, new String[]{"IMS-ALG<>IMS AGW"}, "Provides information to allocate, modify and release media paths between the IP-CAN and IMS core.", new String[]{""}),
    ISC(new String[]{"SIP"}, new String[]{"S-CSCF<>AS","S-CSCF<>MRB"}, "The IMS Service Control Interface is used for requesting services from an AS. Also supports AS initiated requests to the IMS. Used by the MRB to initiate sessions that originated with an AS requesting media resources.", new String[]{"23.228","23.218"}),
    Izi(new String[]{"RTP"}, new String[]{"TrGW <> TrGW belonging to a different IM CN subsystem network"}, "Forwarding media streams from a TrGW to another TrGW belonging to a different IMS network.", new String[]{"29.165"}),
    Ma(new String[]{"SIP"}, new String[]{"AS <> I-CSCF"}, "Supports forwarding of SIP requests destined to a Public Service Identity hosted by an AS.", new String[]{"23.228"}),
    Mb(new String[]{"SIP"}, new String[]{"MGW <> MRFP"}, "IMS Subsystem access to IPv6 networks", new String[]{"23.002","29.162"}),
    Mg(new String[]{"SIP"}, new String[]{"MGCF <> CSCF"}, "Supports incoming/outgoing SIP sessions from the PSTN interworked through the MGCF.", new String[]{"23.002","23.517"}),
    Mi(new String[]{"SIP"}, new String[]{"CSCF <> BGCF"}, "Exchanges messages between S-CSCF and BGCF for interworking with the PSTN.", new String[]{"23.002","23.517"}),
    Mj(new String[]{"SIP"}, new String[]{"BGCF <> MGCF"}, "Exchange of sessions with an MGCF that the BGCF has selected to provide session breakout to the PSTN.,", new String[]{"23.002","23.517"}),
    Mk(new String[]{"SIP"}, new String[]{"BGCF/IMS ALG <> BGCF"}, "Supports interworking with the PSTN/CS domain; used when the BGCF has determined that a breakout should occur in another IMS network. Carries SIP messages from BGCF to a BGCF in another network.", new String[]{"23.002","23.517"}),
    Mm(new String[]{"SIP"}, new String[]{"CSCF <> IBCF"}, "Supports exchange of messages between IMS and external IP networks.", new String[]{"23.002","23.228"}),
    Mn(new String[]{"H.248"}, new String[]{"MCGF <> IMS MGW"}, "Supports exchange of messages between an MGCF and IMS MGW for control of bearer channels between the IMS and PSTN.", new String[]{"23.002","23.517"}),
    Mp(new String[]{"H.248"}, new String[]{"MRFC <> MRFP"}, "Supports MRFC control of media stream resources provided by an MRF.", new String[]{"23.002","23.517"}),
    Mr(new String[]{"SIP"}, new String[]{"CSCF <> MRFC","AS <> MRFC"}, "Supports exchange of information between an S-CSCF and MRFC. Also carries session control messages between an AS and MRFC.", new String[]{"23.002","23.517"}),
    MrPrime(new String[]{"SIP"}, new String[]{"AS <> MRFC"}, "Supports interaction between an AS and an MRFC for session control without passing through an S-CSCF", new String[]{"23.218"}),
    Mw(new String[]{"SIP"}, new String[]{"CSCF <> CSCF"}, "Supports exchange of messages between core CSCFs.", new String[]{"23.002","23.517"}),
    Mx(new String[]{"SIP"}, new String[]{"CSCF/BGCF <> IBCF"}, "Provides interworking with another IMS network when the BGCF has determined that a breakout should occur into the other IMS network. Carries SIP message from the BGCF to the IBCF in the other network.", new String[]{"23.002","23.517"}),
    Mz(new String[]{"Diameter"}, new String[]{"VPLMN BM-SC <> HPLMN BM-SC"}, "Supports exchange of signaling between the GGSN and the Broadcast-Multicast Service Center for MBMS users. This is the roaming version of the Gmb interface.", new String[]{""}),
    N26(new String[]{"GTPv2-C"}, new String[]{"MME <> AMF"}, "N26 interface is an inter-CN interface between the MME and 5GS AMF in order to enable interworking between EPC and the NG core", new String[]{"23.501"}),
    Pr(new String[]{"Diameter"}, new String[]{"AAA Server <> PNA"}, "Used for reporting IP connectivity related events.", new String[]{""}),
    Rc(new String[]{"SIP"}, new String[]{"AS <> MRB"}, "Supports requests by AS for assignment of media resources for a call when utilizing an MRB in In-Line mode or Query mode", new String[]{"23.218"}),
    Re(new String[]{"Diameter"}, new String[]{"Charging Function <> Rating function"}, "Conveys pricing and tariff information from the Rating function.,", new String[]{"32.296"}),
    Rf(new String[]{"Diameter"}, new String[]{"AS <> OFCS"}, "Supports exchange of offline charging information.", new String[]{""}),
    Ro(new String[]{"Diameter"}, new String[]{"AS <> OCS"}, "Supports exchange of online charging in both the IMS and EPC.", new String[]{""}),
    Rx(new String[]{"Diameter"}, new String[]{"P-CSCF <> PCRF"}, "Used to exchange policy and charging related information.", new String[]{""}),
    S10(new String[]{"GTPv2-C"}, new String[]{"MME<>MME"}, "Supports exchange of messages for MME relocation and MME-to-MME information transfer.", new String[]{"23.401"}),
    S11(new String[]{"GTPv2-C"}, new String[]{"MME <> SGW"}, "Provides mobility and bearer management for devices.", new String[]{"23.401","29.274"}),
    S12(new String[]{"GTPv1-U"}, new String[]{"RNC"}, "Supports user plane tunneling when a Direct Tunnel is established.", new String[]{"23.401","29.281"}),
    S13(new String[]{"Diameter"}, new String[]{"MME <> EIR"}, "Used for UE IMEI identity check procedures between an MME and EIR.", new String[]{"29.272"}),
    S13Prime(new String[]{"Diameter"}, new String[]{"SGSN <> EIR"}, "Used for UE IMEI identity check procedures between SGSN and EIR.", new String[]{"29.272"}),
    S16(new String[]{"GTP-C"}, new String[]{"SGSN <> SGSN"}, "Interface between two SGSNs within the same or different PLMNs when those SGSNs support S4.", new String[]{"29.274"}),
    S1_MME(new String[]{"NAS","S1-AP"}, new String[]{"eNode B <> MME"}, "Conveys mobility, session and E-UTRAN radio access bearer management messages.", new String[]{"23.002","23.401"}),
    S1_U(new String[]{"GTPv1-U"}, new String[]{"eNode B <> SGW"}, "Bearer user plane tunnel management.", new String[]{""}),
    S2a(new String[]{"GTPv2-C"}, new String[]{"PDN GW <> SGW"}, "Provides user plane control and mobility support between the mobile packet core and WLAN access networks.", new String[]{"29.275","23.402"}),
    S2b(new String[]{"GTPv2-C"}, new String[]{"PDN GW<> ePDG"}, "Provides user plane control and mobility support between the mobile packet core and WLAN access networks.", new String[]{"29.274","23.402"}),
    S3(new String[]{"GTPv2-C"}, new String[]{"SGSN<>MME"}, "Supports control plane message exchange for inter-access network mobility procedures.", new String[]{"23.401","23.002"}),
    S4(new String[]{"GTPv2-C"}, new String[]{"SGSN <> SGW"}, "Supports procedures for setting up and releasing bearers when the UE is in a UMTS network.", new String[]{"23.401","23.002"}),
    S5(new String[]{"GTPv2-C","GTPv1-U"}, new String[]{"SGW <> PDN GW"}, "Provides user plane tunneling and control plane tunnel management for intra-PLMN traffic.", new String[]{"23.401","23.002"}),
    S6a(new String[]{"Diameter"}, new String[]{"MME <> HSS"}, "Supports transfer of subscription and authentication data for authenticating and authorizing user access.", new String[]{"29.272"}),
    S6b(new String[]{"Diameter"}, new String[]{"AAA Server/Proxy <> PDN GW"}, "Used to authenticate and authorize the UE for access", new String[]{"29.273","23.402"}),
    S6d(new String[]{"Diameter"}, new String[]{"SGSN <> HSS"}, "Supports transfer of subscription and authentication data for authenticating and authorizing user access.", new String[]{"29.272"}),
    S8(new String[]{"GTP-U","GTP-C"}, new String[]{"SGW in the VPLMN <> PDN GW in the HPLMN"}, "Provides user plane tunneling and control plane tunnel management for inter-PLMN traffic (roaming).", new String[]{"23.401","23.002"}),
    S9(new String[]{"Diameter"}, new String[]{"PCRF in the HPLMN (H-PCRF) <> PCRF in the VPLMN (V- PCRF)"}, "Supports transfer of (QoS) policy and charging control information from the HPLMN to a VPLMN.", new String[]{"23.203","29.215"}),
    Sd(new String[]{"Diameter"}, new String[]{"PCRF <> TDF"}, "Enables dynamic control over applications through detection, monitoring and reporting procedures.", new String[]{""}),
    SGi(new String[]{"Diameter"}, new String[]{"PDN GW <> Packet Data Network"}, "Interface between EPC and external public or private IP networks (for example, the Internet or IMS network).", new String[]{"29.061","23.002"}),
    Sh(new String[]{"Diameter"}, new String[]{"AS (SIP AS or OSA CSCF) <> HSS"}, "Supports exchange of User Profile information such as service related information, user location information or charging function info.", new String[]{""}),
    Si(new String[]{"Diameter"}, new String[]{"IM-SSF <> HSS"}, "Transports CAMEL subscription information including triggers for use by CAMEL based application services.", new String[]{"23.228"}),
    Sp(new String[]{"Diameter"}, new String[]{"PCRF <> SPR"}, "Carries subscription information and updates in the SPR to the PCRF.,", new String[]{"23.203"}),
    STa(new String[]{"Diameter"}, new String[]{"Trusted non-IP access <> AAA Server/Proxy"}, "Supports secure transport of access authentication, authorization, mobility parameters and charging-related information.", new String[]{"29.273","23.402"}),
    SWa(new String[]{"Diameter"}, new String[]{"Un-trusted non-IP access <> AAA Server/Proxy"}, "Supports secure transport of access authentication, authorization, mobility parameters and charging-related information.", new String[]{"29.273"}),
    SWd(new String[]{"Diameter"}, new String[]{"AAA Proxy <> AAA Server"}, "Carries Authentication, Authorization and associated information between the AAA proxy and the AAA server.", new String[]{"29.273"}),
    SWm(new String[]{"Diameter"}, new String[]{"AAA Server/Proxy <> ePDG"}, "Provides transport of mobility parameters, tunnel authentication and authorization data.", new String[]{"29.273"}),
    SWn(new String[]{"Diameter"}, new String[]{"Untrusted Non-IP Access <> ePDG"}, "UE-initiated tunnel towards ePDG.", new String[]{"29.273","23.402"}),
    SWx(new String[]{"Diameter"}, new String[]{"AAA Server <> HSS"}, "Provides transport of authentication data.", new String[]{""}),
    Wa(new String[]{"RADIUS"}, new String[]{"WLAN Access Network <> AAA Proxy"}, "Provides transport of authentication, authorization and charging data.", new String[]{"29.234","23.234"}),
    Wd(new String[]{"RADIUS"}, new String[]{"AAA Proxy <> AAA Server"}, "Provides transport of authentication, authorization and related subscriber information.", new String[]{"29.234","23.234"}),
    Wg(new String[]{"Diameter"}, new String[]{"AAA Server/Proxy <> WAG"}, "Provides transport and tunneling of charging information.", new String[]{"29.234","23.234"}),
    Wi(new String[]{"IP"}, new String[]{"PDG <> Packet Data Network"}, "Interface from WLAN to a public or private packet data network.", new String[]{"23.234"}),
    Wm(new String[]{"Diameter"}, new String[]{"AAA Server <> PDG"}, "Enables user authentication and authorization along with conveying charging characteristics.", new String[]{"29.234","23.234"}),
    Wx(new String[]{"Diameter"}, new String[]{"AAA Server <> HSS"}, "Provides retrieval of authentication, WLAN subscriber, service and charging information from HSS.", new String[]{"29.234","23.234"}),
    X2(new String[]{"X2AP","GTPv1-U"}, new String[]{"eNode B <> eNode B"}, "Provides support for intra-LTE handovers, context transfer, load management, and user plane bearer transport control between eNode Bs.", new String[]{"23.401","36.420","29.281"}),
    Zh(new String[]{"Diameter","MAP"}, new String[]{"BSF <> HSS"}, "Supports retrieval and transfer of authentication and security information.", new String[]{""}),
    ZhPrime(new String[]{"Diameter","MAP"}, new String[]{"BSF <> HSS"}, "Supports retrieval and transfer of authentication and security information between operators.", new String[]{"33.220","29.109"}),
    Zn(new String[]{"Diameter","HTTP"}, new String[]{"BSF and NAF"}, "Conveys application specific user security settings and key information to the NAF.", new String[]{"33.220","29.109"}),
    ZnPrime(new String[]{"Diameter","HTTP"}, new String[]{"BSF <> Zn Proxy"}, "Conveys intra-operator application specific user security settings and key information to the NAF.", new String[]{"33.220","29.109"});

    private final String description;
    private final List<String> protocols;
    private final List<String> elements;
    private final List<String> references;

    ReferencePoint(final String[] protocols, final String[] elements, final String description, final String[] references) {
        this.protocols = Collections.unmodifiableList(Arrays.asList(protocols));
        this.description = description;
        this.elements = Collections.unmodifiableList(Arrays.asList(elements));
        this.references = Collections.unmodifiableList(Arrays.asList(references));
    }

    public String getDescription() {
        return description;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public List<String> getElements() {
        return elements;
    }

    public List<String> getReferences() {
        return references;
    }
}