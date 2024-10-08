package gateway;

import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Gateway {
    private static final String[] SERVERS = {
            "http://localhost:1234",
            "http://localhost:8082",
            "http://localhost:8083"
    };
    private AtomicInteger requestCounter = new AtomicInteger(0);
    private HttpClient client = HttpClient.newHttpClient();

    public void forwardMessage(HttpExchange exchange) throws Exception{

        System.out.println("Mensagem recebida pelo gateway");

        int serverIndex = requestCounter .getAndIncrement() % SERVERS.length;
        String serverUrl = SERVERS[serverIndex];

        if(exchange.getRequestMethod().equals("POST")){
            InputStream is = exchange.getRequestBody();
            Scanner scanner = new Scanner(is);
            String data = scanner.nextLine();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVERS[0] + "/post"))
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            exchange.sendResponseHeaders(result.statusCode(), result.body().getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(result.body().getBytes());
            os.close();


        } else if (exchange.getRequestMethod().equals("GET")) {
            System.out.println("Entrou no if do GET");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVERS[0] + "/get"))
                    .GET()
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            exchange.sendResponseHeaders(result.statusCode(), result.body().getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(result.body().getBytes());
            os.close();

        }else {
            System.out.println("Entrou no else, ERRO 400");
            String response = "Método errado";
            int statusCode = 400;
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
