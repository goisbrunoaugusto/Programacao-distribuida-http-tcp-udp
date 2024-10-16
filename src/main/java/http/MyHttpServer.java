package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import database.DatabasePersistance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MyHttpServer {
    private final DatabasePersistance db = new DatabasePersistance();
    private static final Logger logger = Logger.getLogger(MyHttpServer.class.getName());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String GATEWAY_ADDRESS = "http://localhost:8080";
    public MyHttpServer() throws SQLException {
    }

    private class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            List<String> mensagens ;

            mensagens = db.getMessages();
            Gson gson = new Gson();
            String json = gson.toJson(mensagens);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            try {
                exchange.sendResponseHeaders(200, json.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(json.getBytes());
                os.close();
            }catch (IOException e){
                logger.info("Erro no get message");
            }
        }
    }

    private class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                InputStream is = exchange.getRequestBody();
                Scanner scanner = new Scanner(is);
                String message = scanner.nextLine();
                is.close();


                db.postMessage(message);

                String response = "Mensagem enviada com sucesso";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }catch (IOException e){
                logger.info("Erro no post message");
            }
        }
    }

    private void runServer(int porta) throws IOException {
        logger.info("Iniciando o servidor na porta: " + porta);
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        server.createContext("/get", new GetHandler());
        server.createContext("/post", new PostHandler());
        server.start();
        startHeartbeat(porta);

    }

    private void startHeartbeat(int port) {
        scheduler.scheduleAtFixedRate(() -> {
            try (HttpClient client = HttpClient.newHttpClient()){
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GATEWAY_ADDRESS))
                        .POST(HttpRequest.BodyPublishers.ofString("HEARTBEAT:" + port))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    logger.severe("Erro ao enviar heartbeat: " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                logger.severe("Erro ao enviar heartbeat: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void main (String[] args) throws IOException, SQLException {
        int porta = Integer.parseInt(args[0]);
        MyHttpServer server = new MyHttpServer();
        server.runServer(porta);

    }
}
