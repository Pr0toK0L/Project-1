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

public class GET {

    public static void main(String[] args) {
        // SNMP target information
        String ipAddress = "192.168.192.128";
        int port = 161;
        String community = "public";

        // SNMP OID to query
        String oid = ".1.3.6.1.2.1.1.1";

        // Create TransportMapping and Snmp objects
        DefaultUdpTransportMapping transport;
        Snmp snmp;

        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            // Create Target
            Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/" + port);
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version2c);

            // Create PDU for GET
            PDU pdu = new PDU();
            pdu.setType(PDU.GET);
            pdu.add(new VariableBinding(new OID(oid)));

            // Send the request asynchronously
            ResponseListener listener = new ResponseListener() {
                public void onResponse(ResponseEvent event) {
                    // Process the response
                    PDU response = event.getResponse();
                    if (response == null) {
                        System.out.println("GET timed out");
                    } else {
                        VariableBinding variableBinding = response.get(0);
                        System.out.println(variableBinding.getOid() + ": " + variableBinding.getVariable());
                    }
                }
            };

            snmp.send(pdu, target, null, listener);

            // Wait for the asynchronous response
            Thread.sleep(5000);

            // Clean up resources
            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
