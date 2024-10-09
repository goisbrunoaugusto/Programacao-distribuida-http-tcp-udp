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
                gateway.forwardMessage(exchange);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void runHttpServer() throws IOException {
        System.out.println("Iniciando Gateway Server");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpContext context = server.createContext("/", new RequestHandler());
        server.start();
    }

    public static void main(String[] args) {
        try{
            MyGatewayHttpServer server = new MyGatewayHttpServer();
            server.runHttpServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
