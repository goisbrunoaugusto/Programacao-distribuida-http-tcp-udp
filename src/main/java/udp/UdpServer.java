package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer {
    public static void udpServer(String port) throws SocketException {
        System.out.println("##### Servidor iniciado na porta: " + port + "#####");
        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));

        try{
            byte[] receiveMessage = new byte[1024];
            while(true){
                DatagramPacket receivedPacket = new DatagramPacket(receiveMessage, receiveMessage.length);
                serverSocket.receive(receivedPacket);
                new Thread(new MessageHandler(receivedPacket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(!serverSocket.isClosed()){
                serverSocket.close();
            }
        }
    }
    public static void main(String[] args) throws SocketException {
            udpServer(args[0]);
    }
}
