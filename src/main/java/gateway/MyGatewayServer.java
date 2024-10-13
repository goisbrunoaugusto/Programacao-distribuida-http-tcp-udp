package gateway;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MyGatewayServer {
    private static final Logger logger = Logger.getLogger(MyGatewayServer.class.getName());
    private static AtomicInteger requestCounter = new AtomicInteger(0);
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final byte[] buffer = new byte[1024];
    private static final int GATEWAYPORT = 8081;
    private static final String[] UDPSERVERS = {
            "127.0.0.1",
    };
    private static final int[] UDPPORTS = {
            2345,
    };

    private static void runUdp(){
        try{
            DatagramSocket socket = new DatagramSocket(GATEWAYPORT);
            logger.info("#### Gateway UDP iniciado na porta: " + socket.getLocalPort() + "####");

            while(true){

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                logger.info("Informações do pacote: " + message);

                executor.submit(() -> udpHandler(socket, packet));

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void udpHandler(DatagramSocket socket, DatagramPacket receivedPacket){
        int serverIndex = requestCounter.getAndIncrement() % UDPSERVERS.length;
        String address = UDPSERVERS[serverIndex];
        int port = UDPPORTS[serverIndex];
        InetAddress receivedAddress = receivedPacket.getAddress();
        int receivedPort = receivedPacket.getPort();

        String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
        logger.info("Pacote recebido pelo handler: " + message);

        try{
            DatagramPacket toServer = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(),
                    InetAddress.getByName(address), port);
            String mensagem = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
            logger.info("Enviando pacote para o servidor: " + mensagem);
            socket.send(toServer);

            DatagramPacket fromServer = new DatagramPacket(buffer, buffer.length);
            socket.receive(fromServer);

            logger.info("Pacote recebido do servidor, tentando enviar para o cliente " );
            DatagramPacket toClient = new DatagramPacket(fromServer.getData(), fromServer.getLength(),
                    receivedAddress, receivedPort);
            socket.send(toClient);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runTcp(){

    }

    private static void runServers(){
        logger.info("Executando servidores");
        new Thread(MyGatewayServer::runUdp).start();
//        executor.submit(MyGatewayServer::runTcp);

    }


    public static void main(String[] args) throws SocketException {
        runServers();
    }

}
