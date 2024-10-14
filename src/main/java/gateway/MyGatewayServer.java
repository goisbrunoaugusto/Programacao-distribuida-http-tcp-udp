package gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static final int UDPGATEWAYPORT = 8081;
    private static final int TCPGATEWAYPORT = 8082;
    private static final int TCPSERVERGATEWAYPORT = 8083;
    private static final String[] UDPSERVERS = {
            "127.0.0.1",
    };
    private static final String[] TCPSERVERS = {
            "127.0.0.1",
    };
    private static final int[] TCPPORTS = {
            2346,
    };
    private static final int[] UDPPORTS = {
            2345,
    };

    private static void runUdp(){
        try{
            DatagramSocket socket = new DatagramSocket(UDPGATEWAYPORT);
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
        int serverIndex = requestCounter.getAndIncrement() % UDPPORTS.length;
        String address = UDPSERVERS[0];
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
        try {
            ServerSocket serverSocket = new ServerSocket(TCPGATEWAYPORT);
            logger.info("#### Gateway TCP iniciado na porta: " + serverSocket.getLocalPort() + "####");


            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Conexão aceita");

                executor.execute(() -> tcpHandler(clientSocket));

            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    private static void tcpHandler(Socket clientSocket){
        int serverIndex = requestCounter.getAndIncrement() % TCPPORTS.length;
        String address = TCPSERVERS[0];
        int port = TCPPORTS[serverIndex];

        try(Socket serverSocket = new Socket(address, port);
            InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream();
            InputStream serverInput = serverSocket.getInputStream();
            OutputStream serverOutput = serverSocket.getOutputStream();)
        {
            logger.info("Conexão com o servidor estabelecida");

            try {
                logger.info("Enviando dados do cliente para o servidor");
                byte[] buffer = new byte[1024];
                int bytesRead;
                bytesRead = clientInput.read(buffer);
                String message = new String(buffer, 0, bytesRead);
                logger.info("Mensagem do cliente: " + message);
                serverOutput.write(buffer, 0, bytesRead);
                serverOutput.flush();

            } catch (IOException e) {
                logger.severe("Erro ao enviar dados do cliente para o servidor: " + e.getMessage());
            }

            try {
                logger.info("Enviando dados do servidor para o cliente");
                byte[] buffer = new byte[1024];
                int bytesRead;
                bytesRead = serverInput.read(buffer);
                clientOutput.write(buffer, 0, bytesRead);
                clientOutput.flush();

            } catch (IOException e) {
                logger.severe("Erro ao enviar dados do servidor para o cliente: " + e.getMessage());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runServers(){
        logger.info("Executando servidores");
        new Thread(MyGatewayServer::runUdp).start();
        new Thread(MyGatewayServer::runTcp).start();

    }

    public static void main(String[] args) throws SocketException {
        runServers();
    }

}
