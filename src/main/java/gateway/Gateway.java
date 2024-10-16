package gateway;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class Gateway {
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final HttpClient client = HttpClient.newHttpClient();
    private static Set<Integer> liveServersPorts = ConcurrentHashMap.newKeySet();
    private static final Logger logger = Logger.getLogger(Gateway.class.getName());
    private static int currentPort = 0;
    private static final String SERVER = "http://localhost:";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final String[] PORTS = {
            "1234",
            "1235",
            "1236",
            "1237",
            "1238"
    };

    public Gateway() {
        resetAliveServers();
    }

    public void forwardMessage(HttpExchange exchange) throws IOException, InterruptedException {

        if(exchange.getRequestMethod().equalsIgnoreCase("POST")){
            InputStream is = exchange.getRequestBody();
            Scanner scanner = new Scanner(is);
            String data = scanner.nextLine();

            if(data.contains("HEARTBEAT")){
                String[] serverAlive = data.split(":");
                liveServersPorts.add(Integer.parseInt(serverAlive[1]));
                logger.info("Servidor " + serverAlive[1] + " está vivo");

                String response = "Mensagem recebida";
                int statusCode = 200;
                exchange.sendResponseHeaders(statusCode, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            getServerIndex();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER + currentPort + "/post"))
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();
            sendRequestandResponse(exchange, request);


        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            getServerIndex();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER + currentPort + "/get"))
                    .GET()
                    .build();
            sendRequestandResponse(exchange, request);

        } else {
            String response = "Método não suportado";
            int statusCode = 400;
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private void getServerIndex(){

        if(liveServersPorts.size() != 0){
            int serverIndex = requestCounter.getAndIncrement() % liveServersPorts.size();
            ArrayList<Integer> setList = new ArrayList<>(liveServersPorts);
            logger.info("########## SERVIDORES NA LISTA" + setList);
            currentPort = setList.get(serverIndex);
            logger.info("Enviando para o servidor na porta: " + currentPort);

        }else if(liveServersPorts.size() == 0){
            logger.severe("Nenhum servidor disponível");
        }
    }

    public void resetAliveServers(){

        scheduler.scheduleAtFixedRate(() -> {
            liveServersPorts.clear();

            logger.info("liveServersPorts foi zerado");
        }, 0, 30, TimeUnit.SECONDS);

    }

    private void sendRequestandResponse(HttpExchange exchange, HttpRequest request) throws java.io.IOException, InterruptedException {
        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        exchange.sendResponseHeaders(result.statusCode(), result.body().getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(result.body().getBytes());
        os.close();
    }
}
