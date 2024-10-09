package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.logging.Logger;

public class UdpServer {
    private static final Logger logger = Logger.getLogger(UdpServer.class.getName());

    public static void udpServer(String port) throws SocketException {
        logger.info("##### Servidor iniciado na porta: " + port + " #####");
        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));

        try {
            while (true) {
                byte[] buffer = new byte[1024];
                logger.info("Entrou no loop infinito");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                logger.info("Pacote recebido pelo servidor udp, enviando para o MessageHandler...");
                new Thread(new MessageHandler(packet)).start();
            }
        }catch (IOException e){
            logger.warning("Erro ao enviar mensagem");
        }
    }

    public static void main(String[] args) throws SocketException {
            udpServer(args[0]);
    }
}
