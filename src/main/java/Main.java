import dataTypes.RData;
import dataTypes.UDPAnswer;
import dataTypes.UDPMessage;
import dataTypes.UDPQuestion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
    public static void main(String[] args) {

        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Received data");

                // Parse incoming message
                UDPMessage message = new UDPMessage(buf);

                System.out.println("Data:");
                System.out.println(message);

                // Calculate question len
                int questionLength = UDPQuestion.parseQuestionLength(buf, 12);

                // Create an answer for query
                byte[] answerSection = new byte[0];
                int answerCount = 0;

                int queryType = message.getQuestion().getType();

                if (queryType == 1) { // record query
                    byte[] domainName = message.getQuestion().getDomainName();

                    //Create Rdata for a record (example ip)
                    byte[] rdata = RData.createA("192.168.1.100");

                    answerSection = UDPAnswer.createAnswer(
                            domainName,    // The domain being answered
                            1,             // Type A
                            1,             // Class IN
                            300,           // TTL (5 minutes)
                            rdata          // The IP address
                    );
                    answerCount = 1;
                }

                byte[] responseHeader = UDPMessage.createUDPHeader(
                        message.getID(),
                        true,                 // QR = true for response
                        message.getOPCODE(),
                        false,                // AA (we're not authoritative)
                        false,                // TC
                        message.isRD(),
                        true,                 // RA (recursion available)
                        message.getZ(),
                        (byte)0,              // RCODE = 0 (no error)
                        message.getQDCOUNT(),
                        answerCount,          // ANCOUNT - number of answers!
                        0,                    // NSCOUNT
                        0                     // ARCOUNT
                );

                // Complete response
                final byte[] bufResponse = new byte[512];
                int pos = 0;

                // header
                System.arraycopy(responseHeader,0,bufResponse,pos,12);
                pos += 12;

                //copy original question
                System.arraycopy(buf,12,bufResponse,pos,questionLength);
                pos += questionLength;

                if(answerCount > 0){
                    System.arraycopy(answerSection,0,bufResponse,pos,answerSection.length);
                    pos += answerSection.length;
                }

                final DatagramPacket packetResponse = new DatagramPacket(
                        bufResponse,
                        pos,
                        packet.getSocketAddress()
                );
                serverSocket.send(packetResponse);

            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
