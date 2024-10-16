package gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MyGatewayServer {
    private static final Logger logger = Logger.getLogger(MyGatewayServer.class.getName());
    private static AtomicInteger requestCounter = new AtomicInteger(0);
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final byte[] buffer = new byte[1024];
    private static final int UDP_GATEWAY_PORT = 8081;
    private static final int TCP_GATEWAY_PORT = 8082;
    private static Set<Integer> liveServersPorts = ConcurrentHashMap.newKeySet();
    private static final String[] UDP_SERVERS = {
            "127.0.0.1",
    };
    private static final String[] TCP_SERVERS = {
            "127.0.0.1",
    };
    private static final int[] TCP_PORTS = {
            2346,
            2347,
            2348,
            2349,
            2350
    };
    private static final int[] UDP_PORTS = {
            2345,
    };

    private static void runUdp(){
        try{
            DatagramSocket socket = new DatagramSocket(UDP_GATEWAY_PORT);
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
        int serverIndex = requestCounter.getAndIncrement() % UDP_PORTS.length;
        String address = UDP_SERVERS[0];
        int port = UDP_PORTS[serverIndex];
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
            ServerSocket serverSocket = new ServerSocket(TCP_GATEWAY_PORT);
            logger.info("#### Gateway TCP iniciado na porta: " + serverSocket.getLocalPort() + "####");


            while (true) {
                Socket clientSocket = serverSocket.accept();

                executor.execute(() -> {
                    tcpHandler(clientSocket);
                });

            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    private static void tcpHandler(Socket clientSocket) {
        try(InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream())
        {
            byte[] clientInputBuffer = new byte[1024];
            int clientInputBytes = clientInput.read(clientInputBuffer);
            String message = new String(clientInputBuffer, 0, clientInputBytes);

            //Recebendo mensagem heartbeat
            if (message.startsWith("HEARTBEAT:")) {
                int serverPort = Integer.parseInt(message.split(":")[1]);
                liveServersPorts.add(serverPort);
                logger.info("Servidor " + serverPort + " está vivo");
                return;
            }

            //Verificando servidores vivos
            String address = TCP_SERVERS[0];
            int port = 0;
            if(liveServersPorts.size() != 0){
                int serverIndex = requestCounter.getAndIncrement() % liveServersPorts.size();
                ArrayList<Integer> setList = new ArrayList<>(liveServersPorts);
                logger.info("########## SERVIDORES NA LISTA" + setList);
                port = setList.get(serverIndex);
                logger.info("Enviando para o servidor na porta: " + port);
            }else if(liveServersPorts.size() == 0){
                logger.severe("Nenhum servidor disponível");
                return;
            }

            try (Socket serverSocket = new Socket(address, port);
                 InputStream serverInput = serverSocket.getInputStream();
                 OutputStream serverOutput = serverSocket.getOutputStream();) {

                try {
                    serverOutput.write(clientInputBuffer, 0, clientInputBytes);
                    serverOutput.flush();

                } catch (IOException e) {
                    logger.severe("Erro ao enviar dados do cliente para o servidor: " + e.getMessage());
                }

                try {
                    byte[] serverInputBuffer = new byte[1024];
                    int serverInputBytes = serverInput.read(serverInputBuffer);
                    clientOutput.write(serverInputBuffer, 0, serverInputBytes);
                    clientOutput.flush();

                } catch (IOException e) {
                    logger.severe("Erro ao enviar dados do servidor para o cliente: " + e.getMessage());
                }

            }
        }catch (IOException e) {
            logger.severe("Erro ao enviar dados do cliente para o servidor: " + e.getMessage());
        }
    }

    private static void runServers(){
        new Thread(MyGatewayServer::runUdp).start();
        new Thread(MyGatewayServer::runTcp).start();

        scheduler.scheduleAtFixedRate(() -> {
            liveServersPorts.clear();

            logger.info("liveServersPorts foi zerado");
        }, 0, 30, TimeUnit.SECONDS);
    }

    private static void clearServersAlive(){

    }

    public static void main(String[] args) throws SocketException {
        runServers();
    }

}
