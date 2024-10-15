package gateway;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Gateway {
    private static final String[] SERVERS = {
            "http://localhost:1234",
            "http://localhost:1235",
            "http://localhost:1236",
            "http://localhost:1237",
            "http://localhost:1238"
    };
    private AtomicInteger requestCounter = new AtomicInteger(0);
    private HttpClient client = HttpClient.newHttpClient();

    public void forwardMessage(HttpExchange exchange) throws IOException, InterruptedException {

        int serverIndex = requestCounter.getAndIncrement() % SERVERS.length;
        String serverUrl = SERVERS[serverIndex];

        if(exchange.getRequestMethod().equalsIgnoreCase("POST")){
            InputStream is = exchange.getRequestBody();
            Scanner scanner = new Scanner(is);
            String data = scanner.nextLine();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVERS[0] + "/post"))
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();
            sendRequestandResponse(exchange, request);


        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVERS[0] + "/get"))
                    .GET()
                    .build();
            sendRequestandResponse(exchange, request);

        }else {
            String response = "Método não suportado";
            int statusCode = 400;
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private void sendRequestandResponse(HttpExchange exchange, HttpRequest request) throws java.io.IOException, InterruptedException {
        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        exchange.sendResponseHeaders(result.statusCode(), result.body().getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(result.body().getBytes());
        os.close();
    }
}
