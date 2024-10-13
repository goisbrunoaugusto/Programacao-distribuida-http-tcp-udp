package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class MyUdpServer {
    private static final Logger logger = Logger.getLogger(MyUdpServer.class.getName());
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static void runServer(String port) throws IOException, SQLException, ExecutionException, InterruptedException {
        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
        byte[] buffer = new byte[1024];
        logger.info("##### Servidor iniciado na porta: " + port + " #####");

        while (true) {

            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(receivedPacket);

            if(receivedPacket.getLength() != 0){
                UdpMessageHandler udpMessageHandler = new UdpMessageHandler(receivedPacket);
                Future<DatagramPacket> futureResponse = executorService.submit(udpMessageHandler);
                DatagramPacket responsePacket = futureResponse.get();

                if(responsePacket != null){
                    responsePacket.setAddress(receivedPacket.getAddress());
                    responsePacket.setPort(receivedPacket.getPort());
                    serverSocket.send(responsePacket);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ExecutionException, InterruptedException {
            runServer(args[0]);
    }
}
