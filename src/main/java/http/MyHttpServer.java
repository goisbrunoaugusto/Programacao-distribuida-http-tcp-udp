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
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class MyHttpServer {
    DatabasePersistance db = new DatabasePersistance();

    public MyHttpServer() throws SQLException {
    }

    private class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> mensagens ;

            mensagens = db.getMessages();
            Gson gson = new Gson();
            String json = gson.toJson(mensagens);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();

        }
    }

    private class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            Scanner scanner = new Scanner(is);
            String message = scanner.nextLine();
            is.close();
            System.out.println(message);

            db.postMessage(message);

            String response = "Mensagem enviada com sucesso";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();


        }
    }

    private void runServer(int porta) throws IOException {
        System.out.println("Iniciando o servidor MyHttpServer");
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        server.createContext("/get", new GetHandler());
        server.createContext("/post", new PostHandler());
        server.start();

    }

    public static void main (String[] args) throws IOException, SQLException {
        int porta = Integer.parseInt(args[0]);
        MyHttpServer server = new MyHttpServer();
        server.runServer(porta);
    }
}
