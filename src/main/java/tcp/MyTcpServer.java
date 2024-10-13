package tcp;

import database.DatabasePersistance;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MyTcpServer {
    private static final Logger logger = Logger.getLogger(MyTcpServer.class.getName());
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final DatabasePersistance db = new DatabasePersistance();

    public MyTcpServer() throws SQLException {
    }

    private void runServer(String port){
        try{
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
            logger.info("##### Servidor iniciado na porta: " + port + " #####");

            while (true){
                Socket socket = serverSocket.accept();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
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
                            executor.execute(() -> postMessage(message, out));

                        }else if (secondLine.equalsIgnoreCase("GET")){
                            logger.info("Mensagem get identificada");
                            executor.execute(() -> {
                                try {
                                    getMessages(out, socket);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        }else{
                            out.println("Método não permitido");
                        }
                    }
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void getMessages(PrintWriter out, Socket socket) throws IOException {
        logger.info("Buscando mensagens");
        List<String> messages = db.getMessages();
        socket.getOutputStream().write(messages.toString().getBytes());
//        socket.close();
//        out.println(messages);
//        out.flush();
//        out.close();
    }

    private void postMessage(String message, PrintWriter out){
        db.postMessage(message);
        out.println("Mensagem enviada com sucesso");
        out.close();
    }


    public static void main(String[] args) throws SQLException {
        MyTcpServer server = new MyTcpServer();
        server.runServer(args[0]);
    }
}
