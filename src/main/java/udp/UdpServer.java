package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class UdpServer {
    private static final Logger logger = Logger.getLogger(UdpServer.class.getName());
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void udpServer() throws SocketException {
//        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
//        logger.info("##### Servidor iniciado na porta: " + port + " #####");
//
//        try {
//            while (true) {
//                byte[] buffer = new byte[1024];
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                serverSocket.receive(packet);
//                logger.info("Pacote recebido pelo servidor udp, enviando para o MessageHandler...");
//                MessageHandler messageHandler = new MessageHandler(packet);
//                logger.info("Menssagem recebida, chamando executorService ");
//                Future<DatagramPacket> futureResponse = executorService.submit(messageHandler);
//                logger.info("Executor service executado, iniciando future.get");
//                DatagramPacket responsePacket = futureResponse.get();
//                serverSocket.send(responsePacket);
//            }
//        }catch (IOException e){
//            logger.warning("Erro ao enviar mensagem");
//        } catch (SQLException | ExecutionException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static void runServer(String port) throws IOException, SQLException, ExecutionException, InterruptedException {
        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
        logger.info("##### Servidor iniciado na porta: " + port + " #####");

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            logger.info("Pacote recebido pelo servidor udp, enviando para o MessageHandler...");
            MessageHandler messageHandler = new MessageHandler(packet);
            logger.info("Menssagem recebida, chamando executorService ");
            Future<DatagramPacket> futureResponse = executorService.submit(messageHandler);
            logger.info("Executor service executado, iniciando future.get");
            DatagramPacket responsePacket = futureResponse.get();
            if(responsePacket != null){
                serverSocket.send(responsePacket);
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ExecutionException, InterruptedException {
//            udpServer(args[0]);
            runServer(args[0]);
    }
}
