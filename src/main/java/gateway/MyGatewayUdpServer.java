package gateway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MyGatewayUdpServer {
    private static final Logger logger = Logger.getLogger(MyGatewayUdpServer.class.getName());
    private static AtomicInteger requestCounter = new AtomicInteger(0);
    private static final String[] SERVERS = {
            "127.0.0.1",
    };
    private static final int[] PORTS = {
            2345,
    };

    private static void udpGateway() throws SocketException {
        DatagramSocket socket = new DatagramSocket(8081);
        logger.info("#### Servidor iniciado na porta: " + socket.getLocalPort() + "####");

        int serverIndex = requestCounter.getAndIncrement() % SERVERS.length;
        String address = SERVERS[serverIndex];
        int port = PORTS[serverIndex];

        try{
            while (true){
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                String[] lines = receivedMessage.split("\n");

                if(lines[1].trim().equalsIgnoreCase("POST") ||
                        lines[1].trim().equalsIgnoreCase("GET")){

                    logger.info("Pacote recebido pelo gateway udp, enviando para o servidor...");
                    DatagramPacket toServer = new DatagramPacket(packet.getData(),
                            packet.getLength(), InetAddress.getByName(address), port);
                    socket.send(toServer);

                    String clientResponse = "Mensagem recebida";
                    DatagramPacket toCLient = new DatagramPacket(clientResponse.getBytes(), clientResponse.getBytes().length,
                            packet.getAddress(), packet.getPort());
                    String clientMessage = new String(toCLient.getData(), 0, toCLient.getLength());
                    logger.info("Pacote recebido pelo gateway udp, enviando para o cliente:: " + clientMessage);
                    socket.send(toCLient);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws SocketException {
        udpGateway();
    }

}
