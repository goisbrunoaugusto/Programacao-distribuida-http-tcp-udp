package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyHttpServer {

    private class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Entrou no getHandler");
            List<String> mensagens = new ArrayList<>();
            String sql = "SELECT * FROM mensagens";

            try (Connection conn = conectar()) {
                assert conn != null;
                System.out.println("Conexao nao nula, fazendo query...");
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    ResultSet rs = pstmt.executeQuery();
                    System.out.println("Query feita com sucesso, adicionando mensagens na lista");
                    while (rs.next()) {
                        mensagens.add(rs.getString("conteudo"));
                    }
                    System.out.println("Mensagens adicionadas, transformando em json");
                    Gson gson = new Gson();
                    String json = gson.toJson(mensagens);
                    System.out.println("Mensagens JSON: " + json);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, json.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(json.getBytes());
                    os.close();

                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            Scanner scanner = new Scanner(is);
            String mensagem = scanner.nextLine();
            is.close();
            System.out.println(mensagem);

            String sql = "INSERT INTO mensagens (conteudo) VALUES (?)";

            try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, mensagem);
                pstmt.executeUpdate();
                System.out.println("Mensagem inserida com sucesso!");
                String response = "Mensagem enviada com sucecsso";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (SQLException e) {
                String response = "Houve um erro ao enviar sua mensagem";
                System.out.println(e.getMessage());
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }
    }


    private Connection conectar() throws SQLException {
        try{
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + System.getenv("DBNAME"), System.getenv("DBUSERNAME"), System.getenv("DBPASSWORD"));
        }catch(SQLException e){
            System.out.println("Erro ao conectar com o BD");
            e.printStackTrace();
            return null;
        }
    }





    private void runServer(int porta) throws IOException {
        System.out.println("Iniciando o servidor MyHttpServer");
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        server.createContext("/get", new GetHandler());
        server.createContext("/post", new PostHandler());
        server.start();

    }
    public static void main (String[] args) throws IOException {
        int porta = Integer.parseInt(args[0]);
        MyHttpServer server = new MyHttpServer();
        server.runServer(porta);
    }
}
