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
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import java.util.*;

public class WALK {

    public static void main(String[] args) {
        // SNMP target information
        String ipAddress = "192.168.192.128";
        int port = 161;
        String community = "public";

        // SNMP OID to walk
        String baseOid = ".1.3.6.1.2.1.1.1";

        // Create TransportMapping and Snmp objects
        DefaultUdpTransportMapping transport;
        Snmp snmp;
        try {
            // Create TransportMapping and Snmp objects
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            // Create Target
            Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/" + port);
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version2c);

            // Perform walk operation
            List<VariableBinding> resultBindings = new ArrayList<>();
            boolean finished = false;
            OID lastReceivedOid = null;

            while (!finished) {
                PDU requestPdu = new PDU();
                requestPdu.setType(PDU.GETNEXT);
                requestPdu.add(new VariableBinding(new OID(baseOid)));

                ResponseEvent responseEvent = snmp.send(requestPdu, target);
                PDU responsePdu = responseEvent.getResponse();

                if (responsePdu == null) {
                    System.out.println("Walk operation timed out");
                    break;
                } else {
                    VariableBinding receivedBinding = responsePdu.get(0);
                    OID receivedOid = receivedBinding.getOid();
                    Variable receivedVariable = receivedBinding.getVariable();
                    String receivedValue = receivedVariable.toString();

                    if (receivedOid == null || receivedOid.size() < baseOid.length()
                            || !receivedOid.toString().startsWith(baseOid)) {
                        // Reached the end of the walk
                        finished = true;
                    } else {
                        // Process the received value
                        System.out.println(receivedOid + ": " + receivedValue);

                        // Prepare for the next iteration
                        resultBindings.add(receivedBinding);
                        lastReceivedOid = receivedOid;

                        // Set the base OID for the next request
                        baseOid = receivedOid.toString();
                    }
                }
            }

            // Clean up resources
            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
