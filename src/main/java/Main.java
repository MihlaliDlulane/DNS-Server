import dataTypes.UDPMessage;
import dataTypes.UDPQuestion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
  public static void main(String[] args){

     try(DatagramSocket serverSocket = new DatagramSocket(2053)) {
       while(true) {
         final byte[] buf = new byte[512];
         final DatagramPacket packet = new DatagramPacket(buf, buf.length);
         serverSocket.receive(packet);
         System.out.println("Received data");

         UDPMessage message = new UDPMessage(buf);
         byte[] responseHeader = UDPMessage.createUDPHeader(message.getID(), true, message.getOPCODE(), message.isAA(), message.isTC(), message.isRD(),
                                                            message.isRA(), message.getZ(), message.getRCODE(), message.getANCOUNT(), message.getANCOUNT(),
                                                            message.getNSCOUNT(), message.getARCOUNT());

         byte[] question = UDPQuestion.createUDPQuestion("example.com",1,1);

         final byte[] bufResponse = new byte[512];
         System.arraycopy(responseHeader, 0, bufResponse, 0, 12);
         System.arraycopy(question,0,bufResponse,12,question.length);
         final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
         serverSocket.send(packetResponse);
       }
     } catch (IOException e) {
         System.out.println("IOException: " + e.getMessage());
     }
  }
}
