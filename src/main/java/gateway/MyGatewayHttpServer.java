package gateway;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;


public class MyGatewayHttpServer {
    private static class RequestHandler implements HttpHandler {
        Gateway gateway = new Gateway();

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                System.out.println("Enviando mensagem do servidorGateway para o gateway");
                gateway.forwardMessage(exchange);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void runServer() throws IOException {
        System.out.println("Starting Gateway Server");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpContext context = server.createContext("/", new RequestHandler());
        server.start();
    }


    public static void main(String[] args) {
        try{
            MyGatewayHttpServer server = new MyGatewayHttpServer();
            server.runServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
