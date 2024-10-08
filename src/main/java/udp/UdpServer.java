package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServer {
    public static void udpServer(String port){
        System.out.println("##### Servidor iniciado na porta: " + port + "#####");
        DatagramSocket serverSocket = null;
        try{
            serverSocket = new DatagramSocket(Integer.parseInt(port));
            byte[] receiveMessage = new byte[1024];
            while(true){
                DatagramPacket receivedPacket = new DatagramPacket(receiveMessage, receiveMessage.length);
                serverSocket.receive(receivedPacket);
                new Thread(new MessageHandler(receivedPacket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }
        }
    }
    public static void main(String[] args){
        udpServer(args[0]);
    }
}
