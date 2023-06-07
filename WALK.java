package proj1;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class WALK {

    public static void main(String[] args) {
        // SNMP target information
        String ipAddress = "192.168.192.128";
        int port = 161;
        String community = "public";

        // SNMP OID to walk
        String oid = ".1.3.6.1.2.1.1.1";

        // Create TransportMapping and Snmp objects
        DefaultUdpTransportMapping transport;
        Snmp snmp;
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + port));
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.setType(PDU.GETNEXT);
            pdu.add(new VariableBinding(new OID(oid)));

            boolean finished = false;

            while (!finished) {
                ResponseEvent response = snmp.send(pdu, target);
                PDU responsePDU = response.getResponse();

                if (responsePDU != null) {
                    VariableBinding vb = responsePDU.get(0);
                    OID currentOid = vb.getOid();

                    if (currentOid != null && currentOid.toString().startsWith(oid)) {
                        System.out.println(vb.getOid() + ": " + vb.getVariable());

                        // Prepare next request
                        pdu.clear();
                        pdu.setType(PDU.GETNEXT);
                        pdu.add(new VariableBinding(currentOid));
                    } else {
                        finished = true;
                    }
                } else {
                    System.out.println("GETNEXT timed out");
                    finished = true;
                }
            }

            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
