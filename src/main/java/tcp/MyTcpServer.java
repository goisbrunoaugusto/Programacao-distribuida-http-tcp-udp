package tcp;

import database.DatabasePersistance;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MyTcpServer {
    private static final Logger logger = Logger.getLogger(MyTcpServer.class.getName());
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final DatabasePersistance db = new DatabasePersistance();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String GATEWAY_ADDRESS = "127.0.0.1";
    private static final int GATEWAY_PORT = 8082;

    public MyTcpServer() throws SQLException {
    }

    private void runServer(String port){
        try{
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
            logger.info("##### Servidor iniciado na porta: " + port + " #####");
            startHeartbeat(port);

            while (true){
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String firstLine;
                if ((firstLine = in.readLine()) != null){

                    if(firstLine.equalsIgnoreCase("Mensagem do cliente")) {
                        logger.info("Mensagem do cliente recebida");

                        String secondLine = in.readLine();

                        if(secondLine.equalsIgnoreCase("POST")){
                            logger.info("Mensagem post identificada");
                            //Lendo a terceira linha
                            String message = in.readLine();
                            executor.execute(() -> {
                                try {
                                    postMessage(message, socket);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        }else if (secondLine.equalsIgnoreCase("GET")){
                            logger.info("Mensagem get identificada");
                            executor.execute(() -> {
                                try {
                                    getMessages(socket);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        }else{
                            socket.getOutputStream().write("Método não permitido".getBytes());
                        }
                    }
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void getMessages(Socket socket) throws IOException {
        logger.info("Buscando mensagens");
        List<String> messages = db.getMessages();
        socket.getOutputStream().write(messages.toString().getBytes());
        socket.close();
    }

    private void postMessage(String message, Socket socket) throws IOException {
        db.postMessage(message);
        socket.getOutputStream().write("Mensagem postada".getBytes());
        socket.close();
    }

    private void startHeartbeat(String port) {
        scheduler.scheduleAtFixedRate(() -> {
            try (Socket socket = new Socket(GATEWAY_ADDRESS, GATEWAY_PORT);
                 OutputStream out = socket.getOutputStream()) {
                String heartbeatMessage = "HEARTBEAT:" + port;
                out.write(heartbeatMessage.getBytes());
                out.flush();
            } catch (IOException e) {
                logger.severe("Erro ao enviar heartbeat: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws SQLException {
        MyTcpServer server = new MyTcpServer();
        server.runServer(args[0]);
    }
}